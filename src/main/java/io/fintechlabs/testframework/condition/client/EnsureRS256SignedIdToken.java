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

import com.google.gson.JsonObject;
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class EnsureRS256SignedIdToken extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 */
	public EnsureRS256SignedIdToken(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = { "id_token", "server", "client" } )
	public Environment evaluate(Environment env) {

		if (!env.containsObject("id_token")) {
			throw error("Couldn't find parsed ID token");
		}

		JsonObject idtoken = env.getObject("id_token");
		if(!"RS256".equals(idtoken.getAsJsonObject("header").get("alg").getAsString())) {
			throw error("ID Token not signed used RS256 algorithm.");
		}
		logSuccess("ID token is signed using RS256");
		return env;

	}

}
