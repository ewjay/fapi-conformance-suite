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

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.openbanking.OBGetResourceEndpoint;
import io.fintechlabs.testframework.openbanking.OBGetResourceEndpoint.Endpoint;
import io.fintechlabs.testframework.testmodule.Environment;


public class CallAccountRequestsEndpointWithBearerToken extends AbstractCondition {

	private static final String ACCOUNT_REQUESTS_RESOURCE = "account-requests";

	private static final Logger logger = LoggerFactory.getLogger(CallAccountRequestsEndpointWithBearerToken.class);

	/**
	 * @param testId
	 * @param log
	 */
	public CallAccountRequestsEndpointWithBearerToken(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = { "access_token", "resource", "account_requests_endpoint_request" })
	@PostEnvironment(required = { "resource_endpoint_response_headers", "account_requests_endpoint_response" })
	public Environment evaluate(Environment env) {

		String accessToken = env.getString("access_token", "value");
		if (Strings.isNullOrEmpty(accessToken)) {
			throw error("Access token not found");
		}

		String tokenType = env.getString("access_token", "type");
		if (Strings.isNullOrEmpty(tokenType)) {
			throw error("Token type not found");
		} else if (!tokenType.equalsIgnoreCase("Bearer")) {
			throw error("Access token is not a bearer token", args("token_type", tokenType));
		}

		String resourceEndpoint = OBGetResourceEndpoint.getBaseResourceURL(env, Endpoint.ACCOUNT_REQUESTS);
		if (Strings.isNullOrEmpty(resourceEndpoint)) {
			throw error("Resource endpoint not found");
		}

		JsonObject requestHeaders = env.getObject("resource_endpoint_request_headers");

		JsonObject requestObject = env.getObject("account_requests_endpoint_request");
		if (requestObject == null) {
			throw error("Couldn't find request object");
		}

		// Build the endpoint URL
		String accountRequestsUrl = UriComponentsBuilder.fromUriString(resourceEndpoint)
			.path(ACCOUNT_REQUESTS_RESOURCE)
			.toUriString();

		try {
			RestTemplate restTemplate = createRestTemplate(env);

			HttpHeaders headers = headersFromJson(requestHeaders);

			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
			headers.setAcceptCharset(Collections.singletonList(Charset.forName("UTF-8")));
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			headers.set("Authorization", String.join(" ", "Bearer", accessToken));


			// Stop RestTemplate from overwriting the Accept-Charset header
			StringHttpMessageConverter converter = new StringHttpMessageConverter();
			converter.setWriteAcceptCharset(false);
			restTemplate.setMessageConverters(Collections.singletonList(converter));

			HttpEntity<String> request = new HttpEntity<>(requestObject.toString(), headers);

			ResponseEntity<String> response = restTemplate.exchange(accountRequestsUrl, HttpMethod.POST, request, String.class);

			String jsonString = response.getBody();

			if (Strings.isNullOrEmpty(jsonString)) {
				throw error("Didn't get back a response from the account requests endpoint");
			} else {
				log("Account requests endpoint response", args("account_requests_endpoint_response", jsonString));

				try {
					JsonElement jsonRoot = new JsonParser().parse(jsonString);
					if (jsonRoot == null || !jsonRoot.isJsonObject()) {
						throw error("Account requests endpoint did not return a JSON object");
					}

					JsonObject responseHeaders = mapToJsonObject(response.getHeaders(), true); // lowercase incoming headers

					env.putObject("account_requests_endpoint_response", jsonRoot.getAsJsonObject());
					env.putObject("resource_endpoint_response_headers", responseHeaders);

					logSuccess("Parsed account requests endpoint response", args("body", jsonString, "headers", responseHeaders));

					return env;
				} catch (JsonParseException e) {
					throw error(e);
				}
			}
		} catch (RestClientResponseException e) {
			throw error("Error from the account requests endpoint", e, args("code", e.getRawStatusCode(), "status", e.getStatusText(), "body", e.getResponseBodyAsString()));
		} catch (NoSuchAlgorithmException | KeyManagementException | CertificateException | InvalidKeySpecException | KeyStoreException | IOException | UnrecoverableKeyException e) {
			logger.warn("Error creating HTTP Client", e);
			throw error("Error creating HTTP Client", e);
		}

	}

}
