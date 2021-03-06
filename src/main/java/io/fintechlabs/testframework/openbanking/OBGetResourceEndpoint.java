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

package io.fintechlabs.testframework.openbanking;

import com.google.common.base.Strings;

import io.fintechlabs.testframework.testmodule.Environment;

public final class OBGetResourceEndpoint {

	public static enum Endpoint {
		ACCOUNT_REQUESTS,
		ACCOUNTS_RESOURCE
	}


	/**
	 * Private constructor.
	 */
	private OBGetResourceEndpoint() {

	}

	/**
	 * Returns the required Endpoint from the current running environment.
	 * Added to allow the user to specify different base Endpoint URIs
	 * for both the Accounts Requests server and the Accounts Resource servers.
	 *
	 * Defaults to returning the "resourceUrl" string.
	 *
	 * @param requiredEndpoint -- AccountRequest or AccountsResource
	 * @return the required Endpoint as a string.
	 */
	public static String getBaseResourceURL(Environment env, Endpoint requiredEndpoint) {
		String resourceEndpoint = env.getString("resource", "resourceUrl");
		String resourceAccountRequest = env.getString("resource","resourceUrlAccountRequests");
		String resourceAccountsResource = env.getString("resource","resourceUrlAccountsResource");

		switch (requiredEndpoint) {
			case ACCOUNT_REQUESTS:
				if (!Strings.isNullOrEmpty(resourceAccountRequest)) {
					return resourceAccountRequest;
				} else {
					return resourceEndpoint;
				}
			case ACCOUNTS_RESOURCE:
				if(!Strings.isNullOrEmpty(resourceAccountsResource)) {
					return resourceAccountsResource;
				} else {
					return resourceEndpoint;
				}
			default:
				return resourceEndpoint;
		}
	}

}
