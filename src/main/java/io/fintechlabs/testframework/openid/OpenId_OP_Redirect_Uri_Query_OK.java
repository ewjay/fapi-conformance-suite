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
import io.fintechlabs.testframework.condition.client.AddNonceToAuthorizationEndpointRequest;
import io.fintechlabs.testframework.condition.client.AddStateToAuthorizationEndpointRequest;
import io.fintechlabs.testframework.condition.client.CheckMatchingCallbackParameters;
import io.fintechlabs.testframework.condition.client.CheckMatchingStateParameter;
import io.fintechlabs.testframework.condition.client.CreateRandomNonceValue;
import io.fintechlabs.testframework.condition.client.CreateRandomStateValue;
import io.fintechlabs.testframework.condition.client.ExtractAuthorizationCodeFromAuthorizationResponse;
import io.fintechlabs.testframework.condition.client.SetAuthorizationEndpointRedirectUri;
import io.fintechlabs.testframework.testmodule.PublishTestModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs test to make sure OP returns authorization code in authorization response
 *
 */
@PublishTestModule(
	testName = "openid-op-redirect-uri-query_ok",
	displayName = "OpenID OP-Redirect_Uri-Query-OK",
	profile = "OIDFBasic",
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.client_secret",
	},
	summary = "Request with a redirect_uri with a query component when a redirect_uri with the same query component is registered"
)
public class OpenId_OP_Redirect_Uri_Query_OK extends OpenIdBaseModule {

	public static Logger logger = LoggerFactory.getLogger(OpenId_OP_Redirect_Uri_Query_OK.class);
	private String redirectUri;

	@Override
	protected void configureDynamicClientRegistration(JsonObject config) {
		redirectUri = env.getString("redirect_uri").concat("?foo=bar");
//		redirectUri = config.get("redirect_uri").getAsString().concat("?foo=bar");
//		config.addProperty("redirect_uri", redirectUri);
		env.putString("redirect_uri", redirectUri);
		exposeEnvString("redirect_uri");
	}

	@Override
	protected void configureAuthorizationRequestParameters() {
		super.configureAuthorizationRequestParameters();

		callAndStopOnFailure(CreateRandomStateValue.class);
		exposeEnvString("state");
		callAndStopOnFailure(AddStateToAuthorizationEndpointRequest.class);

		callAndStopOnFailure(CreateRandomNonceValue.class);
		exposeEnvString("nonce");
		callAndStopOnFailure(AddNonceToAuthorizationEndpointRequest.class);

		callAndStopOnFailure(SetAuthorizationEndpointRedirectUri.class);
	}

	@Override
	protected void handleCustomCallback() {
		callAndStopOnFailure(CheckMatchingCallbackParameters.class);
		// TODO check to make sure redirect_uri's entire string matches
		callAndStopOnFailure(ExtractAuthorizationCodeFromAuthorizationResponse.class);
		callAndStopOnFailure(CheckMatchingStateParameter.class);

	}
}
