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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class EnsureScopeClaimsInUserInfo extends AbstractEnsureClaimsInObject {

	/**
	 * @param testId
	 * @param log
	 */
	public EnsureScopeClaimsInUserInfo(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}


	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(strings = { "userinfo_endpoint_response" }, required = "authorization_endpoint_request")
	public Environment evaluate(Environment env) {
		JsonObject request = env.getObject("authorization_endpoint_request");
		JsonObject userinfo = new JsonParser().parse(env.getString("userinfo_endpoint_response")).getAsJsonObject();

		String reqScopes = env.getString("authorization_endpoint_request", "scope");
		if(!Strings.isNullOrEmpty(reqScopes)) {
			String[] scopes = reqScopes.split("\\s");
			for(String scope : scopes) {
				if(!checkScopeClaimsInObject(userinfo, scope)) {
					throw error("Unable to find claims for scope in userinfo", args("scope", scope, "userinfo", userinfo));
				}
			}
		}
		logSuccess("Found all requested scope claims in userinfo", args("scope", reqScopes, "userinfo", userinfo));
		return env;
	}

}
