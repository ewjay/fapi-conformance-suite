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

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

public class CallDynamicRegistrationEndpointExpectingError extends AbstractCondition {

	private static final Logger logger = LoggerFactory.getLogger(CallDynamicRegistrationEndpointExpectingError.class);

	/**
	 * @param testId
	 * @param log
	 */
	public CallDynamicRegistrationEndpointExpectingError(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = {"server", "dynamic_registration_request"})
	public Environment evaluate(Environment env) {

		if (env.getString("server", "registration_endpoint") == null) {
			throw error("Couldn't find registration endpoint");
		}

		if (!env.containsObject("dynamic_registration_request")){
			throw error("Couldn't find dynamic registration request");
		}

		JsonObject requestObj = env.getObject("dynamic_registration_request");

		try {

			RestTemplate restTemplate = createRestTemplate(env);
			HttpHeaders headers = new HttpHeaders();

			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
			headers.setAcceptCharset(Collections.singletonList(Charset.forName("UTF-8")));
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<?> request = new HttpEntity<>(requestObj.toString(), headers);

			String jsonString = null;

			try {
				jsonString = restTemplate.postForObject(env.getString("server", "registration_endpoint"), request, String.class);
			} catch (RestClientResponseException e) {

				String expectedError = env.getString("expected_dynamic_registration_error");
				if(!Strings.isNullOrEmpty(expectedError)) {
					JsonElement jsonRoot = new JsonParser().parse(e.getResponseBodyAsString());
					if (jsonRoot == null || !jsonRoot.isJsonObject()) {
						throw error("Registration Endpoint Error did not return a JSON object");
					}


					if (jsonRoot.getAsJsonObject().has("error") ) {

						String error = jsonRoot.getAsJsonObject().get("error").getAsString();

						if (!Strings.isNullOrEmpty(error) ) {

							if(error.equals(expectedError)) {
								logSuccess("Dynamic registration error matches expected error",
									args("error", error,
										"expected error", expectedError));

							} else {
								logFailure("Dynamic registration error does not match expected error",
									args("error", error,
										"expected error", expectedError));
							}
							return env;
						}
					} else {
						logFailure("Registration endpoint error response did not return an error code", jsonRoot.getAsJsonObject());
					}
				} else {
					logSuccess("Registration endpoint returned error", args("code", e.getRawStatusCode(), "status", e.getStatusText(), "body", e.getResponseBodyAsString()));
				}
				return env;
			}

			if (Strings.isNullOrEmpty(jsonString)) {
				throw error("Didn't get back a response from the registration endpoint");
			} else {
				log("Registration endpoint response", args("dynamic_registration_response", jsonString));

				try {
					JsonElement jsonRoot = new JsonParser().parse(jsonString);
					if (jsonRoot == null || !jsonRoot.isJsonObject()) {
						throw error("Registration Endpoint did not return a JSON object");
					}

					logSuccess("Parsed registration endpoint response", jsonRoot.getAsJsonObject());

					env.putObject("client", jsonRoot.getAsJsonObject());

					if (jsonRoot.getAsJsonObject().has("registration_client_uri") &&
						jsonRoot.getAsJsonObject().has("registration_access_token")) {

						String registrationClientUri = jsonRoot.getAsJsonObject().get("registration_client_uri").getAsString();
						String registrationAccessToken = jsonRoot.getAsJsonObject().get("registration_access_token").getAsString();

						if (!Strings.isNullOrEmpty(registrationClientUri) &&
							!Strings.isNullOrEmpty(registrationAccessToken)) {
							env.putString("registration_client_uri", registrationClientUri);
							env.putString("registration_access_token", registrationAccessToken);

							logSuccess("Extracted dynamic registration management credentials",
								args("registration_client_uri", registrationClientUri,
									"registration_access_token", registrationAccessToken));
						}
					}
					return env;
				} catch (JsonParseException e) {
					throw error(e);
				}
			}
		} catch (NoSuchAlgorithmException | KeyManagementException | CertificateException | InvalidKeySpecException | KeyStoreException | IOException | UnrecoverableKeyException e) {
			logger.warn("Error creating HTTP Client", e);
			throw error("Error creating HTTP Client", e);
		}
	}

}
