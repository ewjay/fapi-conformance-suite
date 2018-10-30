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
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

public class AddBearerTokenToUserInfoRequest extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 */
	public AddBearerTokenToUserInfoRequest(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = "access_token")
	@PostEnvironment(required = "userinfo_endpoint_request_headers")
	public Environment evaluate(Environment env) {

		JsonObject accessToken = env.getObject("access_token");

		if (accessToken == null) {
			throw error("Access Token not found");
		}

		JsonObject headers = env.getObject("userinfo_endpoint_request_headers");

		if (headers == null) {
			headers = new JsonObject();
			env.putObject("userinfo_endpoint_request_headers", headers);
		}

		headers.addProperty("Authorization", "Bearer " + accessToken.get("value").getAsString());

		logSuccess("Added authorization bearer header", headers);

		return env;
	}

}
