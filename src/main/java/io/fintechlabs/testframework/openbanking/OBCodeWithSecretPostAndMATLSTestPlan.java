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

package io.fintechlabs.testframework.openbanking;

import io.fintechlabs.testframework.plan.PublishTestPlan;
import io.fintechlabs.testframework.plan.TestPlan;

/**
 * @author ddrysdale
 *
 */
@PublishTestPlan (
	testPlanName = "ob-code-with-secret-post-and-matls-test-plan",
	displayName = "OB: code with secret post and matls Test Plan",
	profile = "OB",
	testModuleNames = {
		"ob-discovery-end-point-verification",
		"ob-code-with-secret-post-and-matls",
		"ob-ensure-matls-required-code-with-secret-post-and-matls",
		"ob-ensure-matching-key-in-authorization-request-code-with-secret-post-and-matls",
		"ob-ensure-redirect-uri-in-authorization-request-code-with-secret-post-and-matls",
		"ob-ensure-registered-certificate-for-authorization-code-code-with-secret-post-and-matls",
		"ob-ensure-registered-redirect-uri-code-with-secret-post-and-matls",
		"ob-ensure-request-object-signature-algorithm-is-not-none-code-with-secret-post-and-matls",
		"ob-user-rejects-authentication-code-with-secret-post-and-matls"
	}
)
public class OBCodeWithSecretPostAndMATLSTestPlan implements TestPlan {

}
