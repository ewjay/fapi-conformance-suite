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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

import java.util.List;

/**
 * @author jricher
 *
 */
public class CheckClientConfiguration extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 */
	public CheckClientConfiguration(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.testmodule.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = "client")
	public Environment evaluate(Environment in) {

		// first make sure we've got a "client" object at all
		if (!in.containsObject("client")) {
			throw error("Couldn't find a client configuration at all");
		}

		List<String> lookFor = ImmutableList.of("client_id", "client_secret");

		for (String key : lookFor) {
			ensureString(in, key);
		}

		logSuccess("Found required client configuration keys", args("keys", lookFor));

		return in;
	}

	private void ensureString(Environment in, String path) {
		String string = in.getString("client", path);
		if (Strings.isNullOrEmpty(string)) {
			throw error("Couldn't find required component", args("path", path));
		}
	}

}
