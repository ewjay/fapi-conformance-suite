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

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jwt.SignedJWT;
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

import java.text.ParseException;

/**
 * @author jricher
 *
 */
public class EnsureSignedIdToken extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 */
	public EnsureSignedIdToken(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
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

		String idToken = env.getString("id_token", "value");

		try {
			// translate stored items into nimbus objects
			SignedJWT jwt = SignedJWT.parse(idToken);
			if(jwt.getHeader().getAlgorithm() == Algorithm.NONE) {
				throw error("ID Token is signed using None algorithm");
			}

		} catch(ParseException e) {
			throw error("Error parsing ID Token", e);
		}


		logSuccess("ID token is signed");
		return env;

	}

}
