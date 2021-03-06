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

package io.fintechlabs.testframework.testmodule;

import io.fintechlabs.testframework.condition.ConditionError;

/**
 * @author jricher
 *
 */
public class TestFailureException extends RuntimeException {

	// this is only used if the "cause" is not a ConditionError
	private String testId = null;

	/**
	 *
	 */
	private static final long serialVersionUID = 7168979969763096442L;

	/**
	 * @param cause
	 */
	public TestFailureException(ConditionError cause) {
		super(cause);
	}

	/**
	 *
	 */
	public TestFailureException(String testId, String msg) {
		super(new RuntimeException(msg));
		this.testId = testId;
	}

	public String getTestId() {
		if (getCause() != null && getCause() instanceof ConditionError) {
			return ((ConditionError) getCause()).getTestId();
		} else {
			return testId;
		}
	}

}
