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

/**
 * @author jricher
 *
 */
public class AddFormBasedBearerToUserInfoRequest extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 */
	public AddFormBasedBearerToUserInfoRequest(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = { "userinfo_endpoint_request_form_parameters", "access_token" })
	@PostEnvironment(required = "userinfo_endpoint_request_form_parameters")
	public Environment evaluate(Environment env) {

		if (!env.containsObject("userinfo_endpoint_request_form_parameters")) {
			throw error("Couldn't find request form");
		}
		JsonObject accessToken = env.getObject("access_token");
		if (accessToken == null) {
			throw error("Access Token not found");
		}

		JsonObject o = env.getObject("userinfo_endpoint_request_form_parameters");
		o.addProperty("access_token", accessToken.get("value").getAsString());
		env.putObject("userinfo_endpoint_request_form_parameters", o);

		logSuccess("Added bearer access token to userinfo_endpoint_request_form_parameters ", o);
		log(o);

		return env;
	}

}
