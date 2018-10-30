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

package io.fintechlabs.testframework.openid;

import io.fintechlabs.testframework.plan.PublishTestPlan;
import io.fintechlabs.testframework.plan.TestPlan;

/**
 * @author jricher
 *
 */
@PublishTestPlan (
	testPlanName = "openid-basic-plan",
	displayName = "OpenID Basic Profile Test Plan",
	profile = "OpenID",
	testModuleNames = {
		"openid-op-response-missing",
		"openid-op-response-code",
		"openid-op-idtoken-c-signature",
		"openid-op-idtoken-rs256",
		"openid-op-idtoken-kid",
		"openid-op-clientauth-basic-dynamic",
		"openid-op-clientauth-secretpost-dynamic",
		"openid-op-userinfo-body",
		"openid-op-userinfo-header",
		"openid-op-userinfo-endpoint",
		"openid-op-userinfo-rs256",
		"openid-op-nonce-noreq-code",
		"openid-op-nonce-code",
		"openid-op-display-page",
		"openid-op-display-popup",
		"openid-op-redirect-uri-missing",
		"openid-op-redirect-uri-notreg",
		"openid-op-redirect-uri-query_added",
		"openid-op-redirect-uri-query_mismatch",
		"openid-op-redirect-uri-query_ok",
		"openid-op-redirect-uri-regfrag",
		"openid-op-scope-all",
		"openid-op-scope-address",
		"openid-op-scope-email",
		"openid-op-scope-phone",
		"openid-op-scope-profile"
	}
)
public class OpenIdBasicTestPlan implements TestPlan {

}
