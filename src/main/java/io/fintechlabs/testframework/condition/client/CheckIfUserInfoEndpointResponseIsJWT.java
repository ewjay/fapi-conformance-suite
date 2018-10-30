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
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class CheckIfUserInfoEndpointResponseIsJWT extends CheckIfUserInfoEndpointResponseError {

	/**
	 * @param testId
	 * @param log
	 */
	public CheckIfUserInfoEndpointResponseIsJWT(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = {"userinfo_endpoint_response_code", "userinfo_endpoint_response_headers"}, strings = "userinfo_endpoint_response")
	public Environment evaluate(Environment env) {

		JsonObject headers = env.getObject("userinfo_endpoint_response_headers");

		String contentType = headers.get("content-type").getAsString();
		if(Strings.isNullOrEmpty(contentType)) {
			throw error("userinfo headers does not contain the content-type header", headers);
		} else {
			if(contentType.indexOf("application/jwt") != -1) {
				log("userinfo headers indicate it is a JWT", headers);
			} else {
				throw error("Userinfo response is not a JWT. content-type = " + contentType, headers);
			}
		}
		return env;
	}

}
