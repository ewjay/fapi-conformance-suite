/*******************************************************************************
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package io.fintechlabs.testframework.condition.client;

import com.google.gson.JsonObject;
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;

/**
 * @author jricher
 *
 */
public class CallUserInfoEndpointWithPost extends AbstractCondition {

	private static final Logger logger = LoggerFactory.getLogger(CallUserInfoEndpointWithPost.class);

	/**
	 * @param testId
	 * @param log
	 */
	public CallUserInfoEndpointWithPost(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = { "server", "userinfo_endpoint_request_form_parameters" })
	@PostEnvironment(required = "userinfo_endpoint_response_headers", strings = "userinfo_endpoint_response")
	public Environment evaluate(Environment env) {

		if (env.getString("server", "userinfo_endpoint") == null) {
			throw error("Couldn't find userinfo endpoint");
		}
		String userfinfoEndpoint = env.getString("server", "userinfo_endpoint");

		if (!env.containsObject("userinfo_endpoint_request_form_parameters")) {
			throw error("Couldn't find request form");
		}

		// build up the form
		JsonObject formJson = env.getObject("userinfo_endpoint_request_form_parameters");
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		for (String key : formJson.keySet()) {
			form.add(key, formJson.get(key).getAsString());
		}

		try {
			HttpMethod resourceMethod = HttpMethod.POST;

			RestTemplate restTemplate = createRestTemplate(env);

			// extract the headers for use (below)
			HttpHeaders headers = headersFromJson(env.getObject("userinfo_endpoint_request_headers"));

//			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
			headers.setAcceptCharset(Collections.singletonList(Charset.forName("UTF-8")));

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

			String jsonString = null;

			try {

				ResponseEntity<String> response = restTemplate.exchange(userfinfoEndpoint, resourceMethod, request, String.class);
				JsonObject responseCode = new JsonObject();
				responseCode.addProperty("code", response.getStatusCodeValue());
				String responseBody = response.getBody();
				JsonObject responseHeaders = mapToJsonObject(response.getHeaders(), true);


				env.putObject("userinfo_endpoint_response_code", responseCode);
				env.putString("userinfo_endpoint_response", responseBody);
				env.putObject("userinfo_endpoint_response_headers", responseHeaders);

				logSuccess("Got a response from the userinfo endpoint with POST method", args("body", responseBody, "headers", responseHeaders, "status_code", responseCode));
				return env;

			} catch (RestClientResponseException e) {

				throw error("Error from the userinfo endpoint", e, args("code", e.getRawStatusCode(), "status", e.getStatusText(), "body", e.getResponseBodyAsString()));
			}
//
//			if (Strings.isNullOrEmpty(jsonString)) {
//				throw error("Didn't get back a response from the userinfo endpoint");
//			} else {
//				log("UserInfo endpoint response",
//					args("userinfo_endpoint_response", jsonString));
//
//				try {
//					JsonElement jsonRoot = new JsonParser().parse(jsonString);
//					if (jsonRoot == null || !jsonRoot.isJsonObject()) {
//						throw error("UserInfo Endpoint did not return a JSON object");
//					}
//
//					logSuccess("Parsed userinfo endpoint response", jsonRoot.getAsJsonObject());
//
//					env.putObject("userinfo_endpoint_response", jsonRoot.getAsJsonObject());
//
//					return env;
//				} catch (JsonParseException e) {
//					throw error(e);
//				}
//			}
		} catch (NoSuchAlgorithmException | KeyManagementException | CertificateException | InvalidKeySpecException | KeyStoreException | IOException | UnrecoverableKeyException e) {
			logger.warn("Error creating HTTP Client", e);
			throw error("Error creating HTTP Client", e);
		}

	}

}
