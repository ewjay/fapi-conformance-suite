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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

import java.text.ParseException;

/**
 * @author jricher
 *
 */
public class CheckIfUserInfoEndpointResponseIsJWS extends CheckIfUserInfoEndpointResponseIsJWT {

	/**
	 * @param testId
	 * @param log
	 */
	public CheckIfUserInfoEndpointResponseIsJWS(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = {"userinfo_endpoint_response_code", "userinfo_endpoint_response_headers"}, strings = "userinfo_endpoint_response")
	public Environment evaluate(Environment env) {

		env = super.evaluate(env);

		String response = env.getString("userinfo_endpoint_response");
		try {
			JWT jwt = JWTParser.parse(response);
			if(!(jwt.getHeader().getAlgorithm() instanceof JWSAlgorithm)) {
				throw error("UserInfo response is not a JWS", jwt.getHeader().toJSONObject());
			}
			log("UserInfo response is a JWS", jwt.getHeader().toJSONObject());
		} catch(ParseException e) {
			throw error("UserInfo JWS pase error", e);
		}

		return env;
	}

}
