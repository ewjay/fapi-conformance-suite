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
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class GenerateJWKsFromClientSecret extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 * @param optional
	 */
	public GenerateJWKsFromClientSecret(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = "client")
	@PostEnvironment(required = "client_jwks")
	public Environment evaluate(Environment env) {
		String clientId = env.getString("client", "client_id");
		String clientSecret = env.getString("client", "client_secret");

		if (Strings.isNullOrEmpty(clientSecret)) {
			throw error("Couldn't find client secret");
		}

		String alg = env.getString("client", "client_secret_jwt_alg");
		if (Strings.isNullOrEmpty(alg)) {
			alg = JWSAlgorithm.HS256.getName();
		}

		// generate a JWK Set for the client's secret
		JWK jwk = new OctetSequenceKey.Builder(clientSecret.getBytes())
			.algorithm(JWSAlgorithm.parse(alg))
			.keyUse(KeyUse.SIGNATURE)
			// no key ID
			.build();

		JWKSet jwks = new JWKSet(jwk);

		JsonObject reparsed = new JsonParser().parse(jwks.toJSONObject(false).toJSONString()).getAsJsonObject();

		env.putObject("client_jwks", reparsed);

		logSuccess("Generated JWK Set from symmetric key", args("client_jwks", reparsed));

		return env;

	}

}
