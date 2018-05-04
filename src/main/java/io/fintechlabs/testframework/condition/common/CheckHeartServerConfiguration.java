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

package io.fintechlabs.testframework.condition.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class CheckHeartServerConfiguration extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 */
	public CheckHeartServerConfiguration(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.testmodule.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = "server")
	public Environment evaluate(Environment in) {

		// first make sure we've got a "server" object at all
		if (!in.containsObj("server")) {
			throw error("Couldn't find a server configuration at all");
		}

		List<String> lookFor = ImmutableList.of("authorization_endpoint", "token_endpoint", "issuer", "introspection_endpoint", "revocation_endpoint", "jwks_uri");

		for (String key : lookFor) {
			ensureString(in, key);

			ensureUrl(in, key);
		}

		logSuccess("Found required server configuration keys", args("required", lookFor));

		return in;
	}

	private void ensureString(Environment in, String path) {
		String string = in.getString("server", path);
		if (Strings.isNullOrEmpty(string)) {
			throw error("Couldn't find required component", args("required", path));
		}
	}

	private void ensureUrl(Environment in, String path) {
		String string = in.getString("server", path);

		try {
				URL url = new URL(string);
		} catch (MalformedURLException e) {
			throw error("Couldn't parse key as URL", e, args("required", path, "url", string));
		}

	}

}
