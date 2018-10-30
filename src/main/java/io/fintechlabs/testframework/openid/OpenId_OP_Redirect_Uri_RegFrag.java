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

package io.fintechlabs.testframework.openid;

import com.google.gson.JsonObject;
import io.fintechlabs.testframework.condition.Condition;
import io.fintechlabs.testframework.condition.client.AddRedirectUriToDynamicRegistrationRequest;
import io.fintechlabs.testframework.condition.client.CallDynamicRegistrationEndpointExpectingError;
import io.fintechlabs.testframework.condition.client.CreateDynamicRegistrationRequest;
import io.fintechlabs.testframework.condition.client.CreateRedirectUri;
import io.fintechlabs.testframework.condition.client.ExtractTLSTestValuesFromServerConfiguration;
import io.fintechlabs.testframework.condition.client.FetchServerKeys;
import io.fintechlabs.testframework.condition.client.GetDynamicServerConfiguration;
import io.fintechlabs.testframework.condition.client.GetStaticClientConfiguration;
import io.fintechlabs.testframework.condition.common.CheckServerConfiguration;
import io.fintechlabs.testframework.condition.common.DisallowTLS10;
import io.fintechlabs.testframework.condition.common.DisallowTLS11;
import io.fintechlabs.testframework.condition.common.EnsureTLS12;
import io.fintechlabs.testframework.testmodule.PublishTestModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs test to make sure OP returns authorization code in authorization response
 *
 */
@PublishTestModule(
	testName = "openid-op-redirect-uri-regfrag",
	displayName = "OpenID OP-Redirect_Uri-RegFrag",
	profile = "OIDFBasic",
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.client_secret",
	},
	summary = "Reject registration where a redirect_uri has a fragment"
)
public class OpenId_OP_Redirect_Uri_RegFrag extends OpenIdBaseModule {

	public static Logger logger = LoggerFactory.getLogger(OpenId_OP_Redirect_Uri_RegFrag.class);
	private String redirectUri;


	@Override
	public void configure(JsonObject config, String baseUrl) {

		env.putString("base_url", baseUrl);
		env.putObject("config", config);

		env.putString("redirect_uri_suffix", "#foobar");
		callAndStopOnFailure(CreateRedirectUri.class);

		// this is inserted by the create call above, expose it to the test environment for publication
		exposeEnvString("redirect_uri");

		// Get the server's configuration
		callAndContinueOnFailure(GetDynamicServerConfiguration.class);
		callAndStopOnFailure(ExtractTLSTestValuesFromServerConfiguration.class);

		// make sure the server configuration passes some basic sanity checks
		callAndStopOnFailure(CheckServerConfiguration.class);

		// fetch or load the server's keys as needed
		callAndStopOnFailure(FetchServerKeys.class);

		// Set up the client configuration
		callAndContinueOnFailure(GetStaticClientConfiguration.class);


		setStatus(Status.CONFIGURED);
		fireSetupDone();
	}


	@Override
	public void start() {
		setStatus(Status.RUNNING);

		eventLog.startBlock("Authorization endpoint TLS test");
		env.mapKey("tls", "authorization_endpoint_tls");
		callAndContinueOnFailure(EnsureTLS12.class, Condition.ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		callAndContinueOnFailure(DisallowTLS10.class, Condition.ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		callAndContinueOnFailure(DisallowTLS11.class, Condition.ConditionResult.FAILURE, "FAPI-RW-8.5-2");

		eventLog.startBlock("Token Endpoint TLS test");
		env.mapKey("tls", "token_endpoint_tls");
		callAndContinueOnFailure(EnsureTLS12.class, Condition.ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		callAndContinueOnFailure(DisallowTLS10.class, Condition.ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		callAndContinueOnFailure(DisallowTLS11.class, Condition.ConditionResult.FAILURE, "FAPI-RW-8.5-2");
//		callAndContinueOnFailure(DisallowInsecureCipher.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");

		eventLog.startBlock("Userinfo Endpoint TLS test");
		env.mapKey("tls", "userinfo_endpoint_tls");
		skipIfMissing(new String[] {"tls"}, null, Condition.ConditionResult.INFO, EnsureTLS12.class, Condition.ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		skipIfMissing(new String[] {"tls"}, null, Condition.ConditionResult.INFO, DisallowTLS10.class, Condition.ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		skipIfMissing(new String[] {"tls"}, null, Condition.ConditionResult.INFO, DisallowTLS11.class, Condition.ConditionResult.FAILURE, "FAPI-RW-8.5-2");
//		skipIfMissing(new String[] {"tls"}, null, ConditionResult.INFO, DisallowInsecureCipher.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");

		eventLog.startBlock("Registration Endpoint TLS test");
		env.mapKey("tls", "registration_endpoint_tls");
		skipIfMissing(new String[] {"tls"}, null, Condition.ConditionResult.INFO, EnsureTLS12.class, Condition.ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		skipIfMissing(new String[] {"tls"}, null, Condition.ConditionResult.INFO, DisallowTLS10.class, Condition.ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		skipIfMissing(new String[] {"tls"}, null, Condition.ConditionResult.INFO, DisallowTLS11.class, Condition.ConditionResult.FAILURE, "FAPI-RW-8.5-2");
//		skipIfMissing(new String[] {"tls"}, null, ConditionResult.INFO, DisallowInsecureCipher.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");

//		eventLog.startBlock("Resource Endpoint TLS test");
//		env.mapKey("tls", "resource_endpoint_tls");
//		callAndContinueOnFailure(EnsureTLS12.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
//		callAndContinueOnFailure(DisallowTLS10.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
//		callAndContinueOnFailure(DisallowTLS11.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
//		callAndContinueOnFailure(DisallowInsecureCipher.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");

		eventLog.endBlock();
		env.unmapKey("tls");

		// make sure we've got a client object
		JsonObject client = env.getObject("client");
		if (client == null || !client.isJsonObject()) {
			// Call Dynamic Client registration
			callAndContinueOnFailure(CreateDynamicRegistrationRequest.class);
			JsonObject dynamicRegistrationRequest = env.getObject("dynamic_registration_request");
			if(dynamicRegistrationRequest != null) {
				dynamicRegistrationRequest.addProperty("grant_types", "authorization_code");
				configureDynamicClientRegistration(dynamicRegistrationRequest);
			}
			env.putObject("dynamic_registration_request", dynamicRegistrationRequest);
			callAndContinueOnFailure(AddRedirectUriToDynamicRegistrationRequest.class);
			env.putString("expected_dynamic_registration_error", "invalid_redirect_uri");
			callAndStopOnFailure(CallDynamicRegistrationEndpointExpectingError.class);
		}

		fireTestFinished();
	}
}
