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
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;
import org.springframework.http.HttpStatus;

/**
 * @author jricher
 *
 */
public class CheckIfUserInfoEndpointResponseError extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 */
	public CheckIfUserInfoEndpointResponseError(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = {"userinfo_endpoint_response_code", "userinfo_endpoint_response_headers"}, strings = "userinfo_endpoint_response")
	public Environment evaluate(Environment env) {

		if (!env.containsObject("userinfo_endpoint_response_code")) {
			throw error("Couldn't find userinfo endpoint response code");
		}

		JsonObject statusObj = env.getObject("userinfo_endpoint_response_code");

		int status = statusObj.get("code").getAsInt();
		if(status != HttpStatus.ACCEPTED.value()) {
			throw error("UserInfo endpoint error response", env.getObject("userinfo_endpoint_response_code"));
		} else {
			if (Strings.isNullOrEmpty(env.getString("userinfo_endpoint_response"))) {
				throw error("No data returned from UserInfo endpoint endpoint response ");
			} else {
				logSuccess("No error from userinfo endpoint");
				return env;
			}
		}


	}

}
