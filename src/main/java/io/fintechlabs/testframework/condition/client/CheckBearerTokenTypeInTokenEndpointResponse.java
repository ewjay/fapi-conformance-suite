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
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class CheckBearerTokenTypeInTokenEndpointResponse extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 */
	public CheckBearerTokenTypeInTokenEndpointResponse(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = "token_endpoint_response")
	public Environment evaluate(Environment env) {

		if (!env.containsObject("token_endpoint_response")) {
			throw error("Couldn't find token endpoint response");
		}

		String tokenType = env.getString("token_endpoint_response", "token_type");
		if (Strings.isNullOrEmpty(tokenType)) {
			throw error("Token endpoint doesn't contain token_type", env.getObject("token_endpoint_response"));
		} else if (!"Bearer".equals(tokenType)) {
			throw error("Token endpoint contains token_type = " + tokenType, env.getObject("token_endpoint_response"));
		} else {
			logSuccess("Token endpoint response contains 'Bearer' token_type");
			return env;
		}

	}

}
