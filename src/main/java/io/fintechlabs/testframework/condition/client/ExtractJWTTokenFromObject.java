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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

import java.text.ParseException;

public class ExtractJWTTokenFromObject extends AbstractCondition {

	public ExtractJWTTokenFromObject(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = "jwt_token")
	@PostEnvironment(required = "jwt_token")
	public Environment evaluate(Environment env) {

		JsonElement jwtTokenElement = env.getElementFromObject("jwt_token", "value");
		if (jwtTokenElement == null || !jwtTokenElement.isJsonPrimitive()) {
			throw error("Couldn't find an JWT Token in response");
		}

		String jwtTokenString = jwtTokenElement.getAsString();

		try {
			JWT jwtToken = JWTParser.parse(jwtTokenString);

			// Note: we need to round-trip this to get to GSON objects because the JWT library uses a different parser
			JsonObject header = new JsonParser().parse(jwtToken.getHeader().toJSONObject().toJSONString()).getAsJsonObject();
			JsonObject claims = new JsonParser().parse(jwtToken.getJWTClaimsSet().toJSONObject().toJSONString()).getAsJsonObject();

			JsonObject o = new JsonObject();
			o.addProperty("value", jwtTokenString); // save the original string to allow for crypto operations
			o.add("header", header);
			o.add("claims", claims);

			// save the parsed ID token
			env.putObject("jwt_token", o);

			logSuccess("Found and parsed the ID Token", o);

			return env;

		} catch (ParseException e) {
			throw error("Couldn't parse JWT", e, args("jwt_tokeng", jwtTokenString));
		}
	}

}
