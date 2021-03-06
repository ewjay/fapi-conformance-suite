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

package io.fintechlabs.testframework.example;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;

import com.google.gson.JsonObject;

import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.condition.as.AddUserinfoUrlToServerConfiguration;
import io.fintechlabs.testframework.condition.as.AuthenticateClientWithClientSecret;
import io.fintechlabs.testframework.condition.as.CreateAuthorizationCode;
import io.fintechlabs.testframework.condition.as.CreateTokenEndpointResponse;
import io.fintechlabs.testframework.condition.as.EnsureClientIsAuthenticated;
import io.fintechlabs.testframework.condition.as.EnsureMatchingClientId;
import io.fintechlabs.testframework.condition.as.EnsureMatchingRedirectUri;
import io.fintechlabs.testframework.condition.as.EnsureMinimumKeyLength;
import io.fintechlabs.testframework.condition.as.ExtractClientCredentialsFromFormPost;
import io.fintechlabs.testframework.condition.as.ExtractNonceFromAuthorizationRequest;
import io.fintechlabs.testframework.condition.as.ExtractRequestedScopes;
import io.fintechlabs.testframework.condition.as.FilterUserInfoForScopes;
import io.fintechlabs.testframework.condition.as.GenerateBearerAccessToken;
import io.fintechlabs.testframework.condition.as.GenerateIdTokenClaims;
import io.fintechlabs.testframework.condition.as.GenerateServerConfiguration;
import io.fintechlabs.testframework.condition.as.LoadServerJWKs;
import io.fintechlabs.testframework.condition.as.RedirectBackToClientWithAuthorizationCode;
import io.fintechlabs.testframework.condition.as.SignIdToken;
import io.fintechlabs.testframework.condition.as.ValidateAuthorizationCode;
import io.fintechlabs.testframework.condition.as.ValidateRedirectUri;
import io.fintechlabs.testframework.condition.client.GetStaticClientConfiguration;
import io.fintechlabs.testframework.condition.common.CheckServerConfiguration;
import io.fintechlabs.testframework.condition.common.EnsureMinimumClientSecretEntropy;
import io.fintechlabs.testframework.condition.rs.ExtractBearerAccessTokenFromHeader;
import io.fintechlabs.testframework.condition.rs.ExtractBearerAccessTokenFromParams;
import io.fintechlabs.testframework.condition.rs.LoadUserInfo;
import io.fintechlabs.testframework.condition.rs.RequireBearerAccessToken;
import io.fintechlabs.testframework.condition.rs.RequireOpenIDScope;
import io.fintechlabs.testframework.testmodule.AbstractTestModule;
import io.fintechlabs.testframework.testmodule.PublishTestModule;
import io.fintechlabs.testframework.testmodule.TestFailureException;

/**
 * @author jricher
 *
 */
@PublishTestModule(
	testName = "sample-client-test",
	displayName = "Sample Client Test",
	configurationFields = {
		"server.jwks",
		"client.client_id",
		"client.client_secret",
		"client.scope",
		"client.redirect_uri"
	}
)
public class SampleClientTestModule extends AbstractTestModule {

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.testmodule.TestModule#configure(com.google.gson.JsonObject, io.fintechlabs.testframework.logging.EventLog, java.lang.String, io.fintechlabs.testframework.frontChannel.BrowserControl, java.lang.String)
	 */
	@Override
	public void configure(JsonObject config, String baseUrl) {
		env.putString("base_url", baseUrl);
		env.putObject("config", config);

		callAndStopOnFailure(GenerateServerConfiguration.class);
		callAndStopOnFailure(AddUserinfoUrlToServerConfiguration.class);
		exposeEnvString("discoveryUrl");
		exposeEnvString("issuer");

		callAndStopOnFailure(CheckServerConfiguration.class);

		callAndStopOnFailure(LoadServerJWKs.class);

		callAndStopOnFailure(EnsureMinimumKeyLength.class, "FAPI-R-5.2.2-5", "FAPI-R-5.2.2-6");

		callAndStopOnFailure(LoadUserInfo.class);

		callAndStopOnFailure(GetStaticClientConfiguration.class);

		callAndContinueOnFailure(EnsureMinimumClientSecretEntropy.class, ConditionResult.FAILURE, "RFC6819-5.1.4.2-2", "RFC6749-10.10");

		setStatus(Status.CONFIGURED);
		fireSetupDone();
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.testmodule.TestModule#start()
	 */
	@Override
	public void start() {
		// TODO Auto-generated method stub

		setStatus(Status.WAITING);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.testmodule.TestModule#handleHttp(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.http.HttpSession, org.springframework.util.MultiValueMap, org.springframework.ui.Model)
	 */
	@Override
	public Object handleHttp(String path, HttpServletRequest req, HttpServletResponse res, HttpSession session, JsonObject requestParts) {

		if (path.equals("authorize")) {
			return authorizationEndpoint(requestParts);
		} else if (path.equals("token")) {
			return tokenEndpoint(requestParts);
		} else if (path.equals("jwks")) {
			return jwksEndpoint();
		} else if (path.equals("register")) {
			return registrationEndpoint(requestParts);
		} else if (path.equals("userinfo")) {
			return userinfoEndpoint(requestParts);
		} else if (path.equals(".well-known/openid-configuration")) {
			return discoveryEndpoint();
		} else {
			throw new TestFailureException(getId(), "Got unexpected HTTP call to " + path);
		}

	}

	/**
	 * @param req
	 * @param res
	 * @param params
	 * @param m
	 * @return
	 */
	private Object discoveryEndpoint() {
		JsonObject serverConfiguration = env.getObject("server");

		return new ResponseEntity<Object>(serverConfiguration, HttpStatus.OK);
	}

	/**
	 * @param req
	 * @param res
	 * @param params
	 * @param m
	 * @return
	 */
	private Object userinfoEndpoint(JsonObject requestParts) {

		setStatus(Status.RUNNING);

		env.putObject("incoming_request", requestParts);

		callAndContinueOnFailure(ExtractBearerAccessTokenFromHeader.class);
		callAndContinueOnFailure(ExtractBearerAccessTokenFromParams.class);

		callAndStopOnFailure(RequireBearerAccessToken.class);

		callAndStopOnFailure(RequireOpenIDScope.class, "FAPI-R-5.2.3-7");

		callAndStopOnFailure(FilterUserInfoForScopes.class);

		JsonObject user = env.getObject("user_info_endpoint_response");

		// at this point we can assume the test is fully done
		fireTestFinished();

		return new ResponseEntity<Object>(user, HttpStatus.OK);

	}

	/**
	 * @param req
	 * @param res
	 * @param params
	 * @param m
	 * @return
	 */
	private Object registrationEndpoint(JsonObject requestParts) {

		//env.putObject("client_registration_request", requestParts.get("body_json"));

		// TODO Auto-generated method stub
		return null;

	}

	/**
	 * @param req
	 * @param res
	 * @param params
	 * @param m
	 * @return
	 */
	private Object jwksEndpoint() {

		setStatus(Status.RUNNING);
		JsonObject jwks = env.getObject("server_public_jwks");

		setStatus(Status.WAITING);

		return new ResponseEntity<Object>(jwks, HttpStatus.OK);
	}

	/**
	 * @param req
	 * @param res
	 * @param params
	 * @param m
	 * @return
	 */
	private Object tokenEndpoint(JsonObject requestParts) {

		setStatus(Status.RUNNING);

		env.putObject("token_endpoint_request", requestParts);

		callAndContinueOnFailure(ExtractClientCredentialsFromFormPost.class);

		callAndContinueOnFailure(AuthenticateClientWithClientSecret.class);

		callAndStopOnFailure(EnsureClientIsAuthenticated.class);

		callAndStopOnFailure(ValidateAuthorizationCode.class);

		callAndStopOnFailure(ValidateRedirectUri.class);

		callAndStopOnFailure(GenerateBearerAccessToken.class);

		callAndStopOnFailure(GenerateIdTokenClaims.class);

		callAndStopOnFailure(SignIdToken.class);

		callAndStopOnFailure(CreateTokenEndpointResponse.class);

		setStatus(Status.WAITING);

		return new ResponseEntity<Object>(env.getObject("token_endpoint_response"), HttpStatus.OK);

	}

	/**
	 * @param req
	 * @param res
	 * @param params
	 * @param m
	 * @return
	 */
	private Object authorizationEndpoint(JsonObject requestParts) {

		setStatus(Status.RUNNING);

		env.putObject("authorization_endpoint_request", requestParts);

		callAndStopOnFailure(EnsureMatchingClientId.class);

		callAndStopOnFailure(EnsureMatchingRedirectUri.class);

		callAndStopOnFailure(ExtractRequestedScopes.class);

		callAndContinueOnFailure(ExtractNonceFromAuthorizationRequest.class);

		callAndStopOnFailure(CreateAuthorizationCode.class);

		callAndStopOnFailure(RedirectBackToClientWithAuthorizationCode.class);

		exposeEnvString("authorization_endpoint_response_redirect");

		String redirectTo = env.getString("authorization_endpoint_response_redirect");

		setStatus(Status.WAITING);

		return new RedirectView(redirectTo, false, false, false);

	}

}
