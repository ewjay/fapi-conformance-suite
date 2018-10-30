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

import io.fintechlabs.testframework.condition.client.EnsureAuthorizationEndpointError;
import io.fintechlabs.testframework.testmodule.PublishTestModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs test to make sure OP returns error when no response_type is sent
 * in authorization request
 *
 */
@PublishTestModule(
	testName = "openid-op-response-missing",
	displayName = "OpenID OP-Response-Missing",
	profile = "OIDFBasic",
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.client_secret",
	},
	summary = "Authorization request missing the response_type parameter"
)
public class OpenId_OP_Response_Missing extends OpenIdBaseModule {

	public static Logger logger = LoggerFactory.getLogger(OpenId_OP_Response_Missing.class);

	@Override
	protected void configureAuthorizationRequestParameters() {
	}

	@Override
	protected void handleCustomCallback() {
		callAndStopOnFailure(EnsureAuthorizationEndpointError.class);
	}


	//	/* (non-Javadoc)
//	 * @see io.bspk.selenium.TestModule#start()
//	 */
//	@Override
//	public void start() {
//
//		setStatus(Status.RUNNING);
//
//		callAndStopOnFailure(CreateAuthorizationEndpointRequestFromClientInformation.class);
//
//		callAndStopOnFailure(CreateRandomStateValue.class);
//		exposeEnvString("state");
//		callAndStopOnFailure(AddStateToAuthorizationEndpointRequest.class);
//
//		callAndStopOnFailure(CreateRandomNonceValue.class);
//		exposeEnvString("nonce");
//		callAndStopOnFailure(AddNonceToAuthorizationEndpointRequest.class);
//
//		callAndStopOnFailure(BuildPlainRedirectToAuthorizationEndpoint.class);
//
//		String redirectTo = env.getString("redirect_to_authorization_endpoint");
//
//		eventLog.log(getName(), args("msg", "Redirecting to authorization endpoint",
//			"redirect_to", redirectTo,
//			"http", "redirect"));
//
//		setStatus(Status.WAITING);
//
//		browser.goToUrl(redirectTo);
//	}

//
//	@UserFacing
//	private Object handleCallback(JsonObject requestParts) {
//
//		getTestExecutionManager().runInBackground(() -> {
//			// process the callback
//			setStatus(Status.RUNNING);
//
//			env.putObject("callback_params", requestParts.get("params").getAsJsonObject());
////			callAndStopOnFailure(CheckIfAuthorizationEndpointError.class);
//
//			callAndStopOnFailure(EnsureAuthorizationEndpointError.class);
//
//
//			fireTestFinished();
//			return "done";
//		});
//
//		return redirectToLogDetailPage();
//
//	}

}
