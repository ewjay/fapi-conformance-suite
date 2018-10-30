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

import io.fintechlabs.testframework.condition.client.AddAddressScopeToAuthorizationEndpointRequest;
import io.fintechlabs.testframework.testmodule.PublishTestModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs test to make sure OP returns authorization code in authorization response
 *
 */
@PublishTestModule(
	testName = "openid-op-scope-address",
	displayName = "OpenID OP-Scope-Address",
	profile = "OIDFBasic",
	configurationFields = {
		"server.discoveryUrl",
		"client.client_id",
		"client.client_secret",
	},
	summary = "UserInfo Endpoint access with GET and bearer header"
)
public class OpenId_OP_Scope_Address extends OpenId_OP_Scope_Base {

	public static Logger logger = LoggerFactory.getLogger(OpenId_OP_Scope_Address.class);


	@Override
	protected void addScopesToAuthorizationEndpointRequest() {
		callAndStopOnFailure(AddAddressScopeToAuthorizationEndpointRequest.class);
	}
}
