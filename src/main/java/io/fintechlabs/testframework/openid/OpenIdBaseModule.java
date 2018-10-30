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
import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.condition.client.AddOpenidScopeToAuthorizationEndpointRequest;
import io.fintechlabs.testframework.condition.client.AddRedirectUriToDynamicRegistrationRequest;
import io.fintechlabs.testframework.condition.client.BuildPlainRedirectToAuthorizationEndpoint;
import io.fintechlabs.testframework.condition.client.CallDynamicRegistrationEndpoint;
import io.fintechlabs.testframework.condition.client.CreateAuthorizationEndpointRequestFromClientInformation;
import io.fintechlabs.testframework.condition.client.CreateDynamicRegistrationRequest;
import io.fintechlabs.testframework.condition.client.CreateRedirectUri;
import io.fintechlabs.testframework.condition.client.ExtractTLSTestValuesFromServerConfiguration;
import io.fintechlabs.testframework.condition.client.FetchServerKeys;
import io.fintechlabs.testframework.condition.client.GetDynamicServerConfiguration;
import io.fintechlabs.testframework.condition.client.GetStaticClientConfiguration;
import io.fintechlabs.testframework.condition.client.SetAuthorizationEndpointRequestResponseTypeToCode;
import io.fintechlabs.testframework.condition.common.CheckClientConfiguration;
import io.fintechlabs.testframework.condition.common.CheckServerConfiguration;
import io.fintechlabs.testframework.condition.common.DisallowTLS10;
import io.fintechlabs.testframework.condition.common.DisallowTLS11;
import io.fintechlabs.testframework.condition.common.EnsureTLS12;
import io.fintechlabs.testframework.testmodule.AbstractTestModule;
import io.fintechlabs.testframework.testmodule.TestFailureException;
import io.fintechlabs.testframework.testmodule.UserFacing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author jricher
 *
 */

public abstract class OpenIdBaseModule extends AbstractTestModule {

	public static Logger logger = LoggerFactory.getLogger(OpenIdBaseModule.class);

	/**
	 * Configure the dynamic client registration request parameters
	 * @param config JSON object that holds the registration options
	 */
	protected void configureDynamicClientRegistration(JsonObject config) {
	}

//	/**
//	 * Get the scope(s) that will be put into the authorization request
//	 * Default will request "openid"
//	 * @return list of scopes to request
//	 */
//	protected String[] configureAuthorizationRequestScope() {
//		return new String[] {"openid"};
//	}

	/**
	 * Configures additional parameters for the authorization request prior to making the request
	 * Default only only "response_type" = "code"
	 */
	protected void configureAuthorizationRequestParameters() {
		callAndStopOnFailure(AddOpenidScopeToAuthorizationEndpointRequest.class);
		callAndStopOnFailure(SetAuthorizationEndpointRequestResponseTypeToCode.class);
	}

	/* (non-Javadoc)
	 * @see io.bspk.selenium.TestModule#configure(com.google.gson.JsonObject)
	 */
	@Override
	public void configure(JsonObject config, String baseUrl) {
		env.putString("base_url", baseUrl);
		env.putObject("config", config);

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
			callAndStopOnFailure(CallDynamicRegistrationEndpoint.class);
		}

		// check client information
		callAndStopOnFailure(CheckClientConfiguration.class);
//		callAndContinueOnFailure(EnsureMinimumClientSecretEntropy.class, ConditionResult.FAILURE, "RFC6819-5.1.4.2-2", "RFC6749-10.10");

//		// add authorization scope to client information
//		client = env.getObject("client");
//		String scopes = String.join(" ", configureAuthorizationRequestScope());
//		client.addProperty("scope", scopes);
//		env.putObject("client", client);

		exposeEnvString("client_id");

		setStatus(Status.CONFIGURED);
		fireSetupDone();
	}


	/* (non-Javadoc)
	 * @see io.bspk.selenium.TestModule#start()
	 */
	@Override
	public void start() {

		setStatus(Status.RUNNING);

		eventLog.startBlock("Authorization endpoint TLS test");
		env.mapKey("tls", "authorization_endpoint_tls");
		callAndContinueOnFailure(EnsureTLS12.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		callAndContinueOnFailure(DisallowTLS10.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		callAndContinueOnFailure(DisallowTLS11.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");

		eventLog.startBlock("Token Endpoint TLS test");
		env.mapKey("tls", "token_endpoint_tls");
		callAndContinueOnFailure(EnsureTLS12.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		callAndContinueOnFailure(DisallowTLS10.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		callAndContinueOnFailure(DisallowTLS11.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
//		callAndContinueOnFailure(DisallowInsecureCipher.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");

		eventLog.startBlock("Userinfo Endpoint TLS test");
		env.mapKey("tls", "userinfo_endpoint_tls");
		skipIfMissing(new String[] {"tls"}, null, ConditionResult.INFO, EnsureTLS12.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		skipIfMissing(new String[] {"tls"}, null, ConditionResult.INFO, DisallowTLS10.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		skipIfMissing(new String[] {"tls"}, null, ConditionResult.INFO, DisallowTLS11.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
//		skipIfMissing(new String[] {"tls"}, null, ConditionResult.INFO, DisallowInsecureCipher.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");

		eventLog.startBlock("Registration Endpoint TLS test");
		env.mapKey("tls", "registration_endpoint_tls");
		skipIfMissing(new String[] {"tls"}, null, ConditionResult.INFO, EnsureTLS12.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		skipIfMissing(new String[] {"tls"}, null, ConditionResult.INFO, DisallowTLS10.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
		skipIfMissing(new String[] {"tls"}, null, ConditionResult.INFO, DisallowTLS11.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
//		skipIfMissing(new String[] {"tls"}, null, ConditionResult.INFO, DisallowInsecureCipher.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");

//		eventLog.startBlock("Resource Endpoint TLS test");
//		env.mapKey("tls", "resource_endpoint_tls");
//		callAndContinueOnFailure(EnsureTLS12.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
//		callAndContinueOnFailure(DisallowTLS10.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
//		callAndContinueOnFailure(DisallowTLS11.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");
//		callAndContinueOnFailure(DisallowInsecureCipher.class, ConditionResult.FAILURE, "FAPI-RW-8.5-2");

		eventLog.endBlock();
		env.unmapKey("tls");

		callAndStopOnFailure(CreateAuthorizationEndpointRequestFromClientInformation.class);

		configureAuthorizationRequestParameters();

		callAndStopOnFailure(BuildPlainRedirectToAuthorizationEndpoint.class);

		String redirectTo = env.getString("redirect_to_authorization_endpoint");

		eventLog.log(getName(), args("msg", "Redirecting to authorization endpoint",
			"redirect_to", redirectTo,
			"http", "redirect"));

		setStatus(Status.WAITING);

		browser.goToUrl(redirectTo);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.TestModule#handleHttp(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.http.HttpSession, org.springframework.util.MultiValueMap, org.springframework.ui.Model)
	 */
	@Override
	public Object handleHttp(String path, HttpServletRequest req, HttpServletResponse res, HttpSession session, JsonObject requestParts) {
		// dispatch based on the path
		if (path.equals("callback")) {
			return handleCallback(requestParts);
		} else {
			throw new TestFailureException(getId(), "Got unexpected HTTP call to " + path);
		}

	}

	protected void handleCustomCallback() {

	}

	@UserFacing
	private Object handleCallback(JsonObject requestParts) {

		getTestExecutionManager().runInBackground(() -> {
			// process the callback
			setStatus(Status.RUNNING);

			env.putObject("callback_params", requestParts.get("params").getAsJsonObject());
			env.putObject("callback_query_params", requestParts.get("params").getAsJsonObject());

			handleCustomCallback();

//			callAndStopOnFailure(CheckIfAuthorizationEndpointError.class);
//
//			callAndStopOnFailure(CheckMatchingStateParameter.class);
//
//			callAndStopOnFailure(ExtractAuthorizationCodeFromAuthorizationResponse.class);

//			callAndStopOnFailure(CreateTokenEndpointRequestForAuthorizationCodeGrant.class);
//
//			callAndStopOnFailure(AddFormBasedClientSecretAuthenticationParameters.class);
			//require(CreateClientAuthenticationAssertionClaims.class);

			//require(SignClientAuthenticationAssertion.class);

			//require(AddClientAssertionToTokenEndpointRequest.class);

//			callAndStopOnFailure(CallTokenEndpoint.class);
//
//			callAndStopOnFailure(CheckIfTokenEndpointResponseError.class);
//
//			callAndStopOnFailure(CheckForAccessTokenValue.class, "FAPI-R-5.2.2-14");
//
//			callAndStopOnFailure(ExtractAccessTokenFromTokenResponse.class);
//
//			callAndStopOnFailure(CheckForScopesInTokenResponse.class, "FAPI-R-5.2.2-15");
//
//			callAndStopOnFailure(ExtractIdTokenFromTokenResponse.class, "FAPI-R-5.2.2-24");
//
//			callAndStopOnFailure(ValidateIdToken.class, "FAPI-R-5.2.2-24");
//
//			callAndStopOnFailure(ValidateIdTokenSignature.class, "FAPI-R-5.2.2-24");
//
//			callAndContinueOnFailure(ValidateSHash.class, "FAPI-RW-5.2.2-4");
//
//			callAndContinueOnFailure(CheckForRefreshTokenValue.class);
//
//			callAndStopOnFailure(EnsureMinimumTokenEntropy.class, "FAPI-R-5.2.2-16");

			// verify the access token against a protected resource

			/*
		callAndStopOnFailure(CreateRandomFAPIInteractionId.class);

		callAndStopOnFailure(SetTLSTestHostToResourceEndpoint.class);

		call(DisallowInsecureCipher.class, "FAPI-RW-8.5-1");

		callAndStopOnFailure(CallAccountsEndpointWithBearerToken.class, "FAPI-R-6.2.1-3");

		call(DisallowAccessTokenInQuery.class, "FAPI-R-6.2.1-4");

		callAndStopOnFailure(CheckForDateHeaderInResourceResponse.class, "FAPI-R-6.2.1-11");

		call(CheckForFAPIInteractionIdInResourceResponse.class, "FAPI-R-6.2.1-12");

		call(EnsureMatchingFAPIInteractionId.class, "FAPI-R-6.2.1-12");

		call(EnsureResourceResponseEncodingIsUTF8.class, "FAPI-R-6.2.1-9");
			 */

			fireTestFinished();
			return "done";
		});

		return redirectToLogDetailPage();

	}
}
