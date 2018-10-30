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
import io.fintechlabs.testframework.condition.client.AddBasicAuthClientSecretAuthenticationParameters;
import io.fintechlabs.testframework.condition.client.AddBearerTokenToUserInfoRequest;
import io.fintechlabs.testframework.condition.client.AddNonceToAuthorizationEndpointRequest;
import io.fintechlabs.testframework.condition.client.AddStateToAuthorizationEndpointRequest;
import io.fintechlabs.testframework.condition.client.CallTokenEndpoint;
import io.fintechlabs.testframework.condition.client.CallUserInfoEndpointWithGet;
import io.fintechlabs.testframework.condition.client.CheckBearerTokenTypeInTokenEndpointResponse;
import io.fintechlabs.testframework.condition.client.CheckForAccessTokenValue;
import io.fintechlabs.testframework.condition.client.CheckIfAuthorizationEndpointError;
import io.fintechlabs.testframework.condition.client.CheckIfTokenEndpointResponseError;
import io.fintechlabs.testframework.condition.client.CheckMatchingStateParameter;
import io.fintechlabs.testframework.condition.client.CreateRandomNonceValue;
import io.fintechlabs.testframework.condition.client.CreateRandomStateValue;
import io.fintechlabs.testframework.condition.client.CreateTokenEndpointRequestForAuthorizationCodeGrant;
import io.fintechlabs.testframework.condition.client.CreateUserInfoEndpointRequest;
import io.fintechlabs.testframework.condition.client.EnsureScopeClaimsInUserInfo;
import io.fintechlabs.testframework.condition.client.ExtractAccessTokenFromTokenResponse;
import io.fintechlabs.testframework.condition.client.ExtractAuthorizationCodeFromAuthorizationResponse;
import io.fintechlabs.testframework.condition.client.ExtractIdTokenFromTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for testing scope claims returned in UserInfo endpoint
 *
 */
public abstract class OpenId_OP_Scope_Base extends OpenIdBaseModule {

	public static Logger logger = LoggerFactory.getLogger(OpenId_OP_Scope_Base.class);

	@Override
	protected void configureDynamicClientRegistration(JsonObject config) {
	}


	protected abstract void addScopesToAuthorizationEndpointRequest();

	@Override
	protected void configureAuthorizationRequestParameters() {
		super.configureAuthorizationRequestParameters();
		callAndStopOnFailure(CreateRandomStateValue.class);
		exposeEnvString("state");
		callAndStopOnFailure(AddStateToAuthorizationEndpointRequest.class);

		callAndStopOnFailure(CreateRandomNonceValue.class);
		exposeEnvString("nonce");

		callAndStopOnFailure(AddNonceToAuthorizationEndpointRequest.class);
		addScopesToAuthorizationEndpointRequest();
	}


	@Override
	protected void handleCustomCallback() {
		callAndStopOnFailure(CheckIfAuthorizationEndpointError.class);

		callAndStopOnFailure(CheckMatchingStateParameter.class);

		callAndStopOnFailure(ExtractAuthorizationCodeFromAuthorizationResponse.class);

		callAndStopOnFailure(CreateTokenEndpointRequestForAuthorizationCodeGrant.class);

		callAndStopOnFailure(AddBasicAuthClientSecretAuthenticationParameters.class);

		callAndStopOnFailure(CallTokenEndpoint.class);

		callAndStopOnFailure(CheckIfTokenEndpointResponseError.class);

		callAndContinueOnFailure(CheckBearerTokenTypeInTokenEndpointResponse.class, "OIDC-Core-3.1.3.3");

		callAndStopOnFailure(CheckForAccessTokenValue.class, "OIDC-CORE-3.1.3.3");

		callAndStopOnFailure(ExtractAccessTokenFromTokenResponse.class);

		callAndStopOnFailure(ExtractIdTokenFromTokenResponse.class, "OIDC-CORE-3.1.3.3");

		callAndStopOnFailure(CreateUserInfoEndpointRequest.class);

		callAndStopOnFailure(AddBearerTokenToUserInfoRequest.class);

		callAndStopOnFailure(CallUserInfoEndpointWithGet.class);

		callAndStopOnFailure(EnsureScopeClaimsInUserInfo.class);
	}
}
