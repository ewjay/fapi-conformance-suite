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
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

public class SetAuthorizationEndpointClientIdFromClientInformation extends AbstractSetAuthorizationEndpointRequestParameter {

	public SetAuthorizationEndpointClientIdFromClientInformation(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	@Override
	@PreEnvironment(required = {"authorization_endpoint_request", "client"})
	@PostEnvironment(required = "authorization_endpoint_request")
	public Environment evaluate(Environment env) {
		if (!env.containsObject("client")) {
			throw error("Couldn't find client configuration");
		}

		String clientId = env.getString("client", "client_id");

		if (Strings.isNullOrEmpty(clientId)) {
			throw error("Couldn't find client ID");
		}

		env =  setParameter(env, "client_id", clientId);
		logSuccess("Set authorization endpoint client_id = " + clientId);
		return env;
	}

}
