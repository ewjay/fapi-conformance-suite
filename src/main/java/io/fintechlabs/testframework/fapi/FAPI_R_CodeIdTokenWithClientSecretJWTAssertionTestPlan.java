package io.fintechlabs.testframework.fapi;

import io.fintechlabs.testframework.plan.PublishTestPlan;
import io.fintechlabs.testframework.plan.TestPlan;

/**
 * @author jheenan
 *
 */
@PublishTestPlan (
	testPlanName = "fapi-r-code-id-token-with-client-secret-jwt-test-plan",
	displayName = "FAPI-R: code id_token with client secret JWT assertion Test Plan",
	profile = "FAPI-R",
	testModuleNames = {
		"fapi-r-code-id-token-with-client-secret-jwt",
		"fapi-r-ensure-redirect-uri-in-authorization-request",
		"fapi-r-ensure-redirect-uri-is-registered",
		"fapi-r-require-pkce",
		"fapi-r-reject-plain-pkce"
	}
)
public class FAPI_R_CodeIdTokenWithClientSecretJWTAssertionTestPlan implements TestPlan {

}
