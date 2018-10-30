package io.fintechlabs.testframework.condition.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

import java.util.Set;

/**
 * @author jricher
 *
 */
public class ExtractOpenIdRequestedClaimsFromAuthorizationRequest extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 * @param conditionResultOnFailure
	 * @param requirements
	 */
	public ExtractOpenIdRequestedClaimsFromAuthorizationRequest(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}


	/**
	 * Returns list of scope claims
	 * @param scope space separated value of scopes
	 * @return array of specific claims specified by the scope
	 */
	JsonObject getScopeClaims(String scope) {
		JsonObject claims = new JsonObject();
		if(!Strings.isNullOrEmpty(scope)) {
			String[] scopes = scope.split("\\s");
			for(String s : scopes) {
				if("profile".equals(s)) {
					claims.add("name", null);
					claims.add("family_name", null);
					claims.add("given_name", null);
					claims.add("middle_name", null);
					claims.add("nickname", null);
					claims.add("preferred_username", null);
					claims.add("profile", null);
					claims.add("picture", null);
					claims.add("website", null);
					claims.add("gender", null);
					claims.add("birthdate", null);
					claims.add("zoneinfo", null);
					claims.add("locale", null);
					claims.add("updated_at", null);
				} else if("email".equals(s)) {
					claims.add("email", null);
					claims.add("email_verified", null);
				} else if("phone".equals(s)) {
					claims.add("phone_number", null);
					claims.add("phone_number_verified", null);
				} else if("address".equals(s)) {
					claims.add("adress", null);
				}
			}
		}
		return claims;
	}

	/**
	 * Adds key/alues from obj2 into obj1. If obj1 already contains the key, it is not added.
	 * @param obj1 object to add values into
	 * @param obj2 object containing values to be added
	 * @return "merged" object (obj1)
	 */
	JsonObject addJsonObjectValues(JsonObject obj1, JsonObject obj2) {
		Set<String> keys = obj2.keySet();
		for(String key : keys) {
			if(obj1.get(key) == null) {
				obj1.add(key, obj2.get(key));
			}
		}
		return obj1;
	}


	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = "authorization_endpoint_request")
	@PostEnvironment(required = "openid_requested_claims")
	public Environment evaluate(Environment env) {

		JsonObject request = env.getObject("authorization_endpoint_request");
		String responseType = env.getString("authorization_endpoint_request", "response_type");


		if (Strings.isNullOrEmpty(responseType)) {
			throw error("Couldn't find 'response_type' in authorization endpoint parameters");
		} else {
			JsonObject rootClaims = new JsonObject();
			JsonObject userinfoClaims = new JsonObject();
			JsonObject idtokenClaims = new JsonObject();

			JsonObject claims = request.getAsJsonObject("claims");
			if(claims != null) {
				if(claims.getAsJsonObject("userinfo") != null) {
					userinfoClaims = claims.getAsJsonObject("userinfo");
				}
				if(claims.getAsJsonObject("id_token") != null) {
					idtokenClaims = claims.getAsJsonObject("id_token");
				}
			}
			String scopes = env.getString("authorization_endpoint_request", "scope");
			JsonObject scopeClaims = getScopeClaims(scopes);
			if ("id_token".equals(responseType)) {
				idtokenClaims = addJsonObjectValues(idtokenClaims, scopeClaims);
			} else {
				userinfoClaims = addJsonObjectValues(userinfoClaims, scopeClaims);
			}
			rootClaims.add("userinfo", userinfoClaims);
			rootClaims.add("id_token", idtokenClaims);
			env.putObject("openid_requested_claims", rootClaims);
			logSuccess("Extracted openid requested claims", rootClaims);
			return env;
		}

	}

}
