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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;

import java.util.Map;

/**
 * @author jricher
 *
 */
public abstract class AbstractEnsureClaimsInObject extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 */
	public AbstractEnsureClaimsInObject(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	JsonObject getScopeClaims(String scope, boolean isEssential) {
		JsonObject claims = new JsonObject();
		JsonElement essential = null;
		if(isEssential) {
			essential = new JsonObject();
			((JsonObject)essential).addProperty("essential", true);
		} else {
			essential = JsonNull.INSTANCE;
		}
		if("profile".equals(scope)) {
			claims.add("name", essential);
			claims.add("family_name", essential);
			claims.add("given_name", essential);
			claims.add("middle_name", essential);
			claims.add("nickname", essential);
			claims.add("preferred_username", essential);
			claims.add("profile", essential);
			claims.add("picture", essential);
			claims.add("website", essential);
			claims.add("gender", essential);
			claims.add("birthdate", essential);
			claims.add("zoneinfo", essential);
			claims.add("locale", essential);
			claims.add("updated_at", essential);
		} else if("email".equals(scope)) {
			claims.add("email", essential);
			claims.add("email_verified", essential);
		} else if("phone".equals(scope)) {
			claims.add("phone_number", essential);
			claims.add("phone_number_verified", essential);
		} else if("address".equals(scope)) {
			claims.add("address", essential);
		}
		return claims;
	}

	/***
	 * Checks to see if claims are present in a given object
	 * @param obj object containing claims to check
	 * @param claimsToCheck object containing claims to check
	 *                      Can be in the format :
	 *                      {
								"userinfo":
								{
									"given_name": {"essential": true},
									"nickname": null,
									"email": {"essential": true},
									"email_verified": {"essential": true},
									"picture": null,
									"http://example.info/claims/groups": null
								},
								"id_token":
								{
									"auth_time": {"essential": true},
									"acr": {"values": ["urn:mace:incommon:iap:silver"] }
								}
							}
	 * @return
	 */

	boolean checkClaimsInObject(JsonObject obj, JsonObject claimsToCheck) {
		int numValidClaimsPresent = 0;
		int numEssentialClaims = 0;
		int numNonEssentialClaims = 0;
		int numEssentialClaimsFound = 0;
		int numNonEssentailClaimsFound = 0;
		boolean isPresent = true;
		if(obj != null && claimsToCheck != null) {
			log("checking claims for ", args("Obj", obj, "Claims To Check", claimsToCheck));
			for(Map.Entry<String, JsonElement> entry : claimsToCheck.entrySet()) {
				String key = entry.getKey();
				JsonElement val = entry.getValue();

				if(val!= null && val.isJsonPrimitive()) {
					logFailure("Invalid claims to check format", args("key", key, "value", val));
				}
				boolean isEssential = false;
				JsonElement value = JsonNull.INSTANCE;
				JsonElement values = JsonNull.INSTANCE;

				if(val != null && !val.isJsonNull()) {
					JsonObject valObj = val.getAsJsonObject();
					JsonElement essential = valObj.get("essential");
					if(essential != null && !essential.isJsonNull()) {
						isEssential = essential.getAsBoolean();
					}
					value = valObj.get("value");
					values = valObj.get("values");
				}
				if(value != null && !value.isJsonNull()) {
					isEssential = true;

					if(value.isJsonPrimitive()) {
						JsonArray valuesArray = new JsonArray();
						valuesArray.add(value.getAsJsonPrimitive());
						values = valuesArray;
						isEssential = true;
					} else {
						logFailure("Invalid claims 'value' to check format", args("key", key, "value", val));
					}
				}
				if(isEssential) {
					++numEssentialClaims;
				} else {
					++numNonEssentialClaims;
				}
				if(obj.has(key)) {
					log("obj contains the claim ", args("key", key, "value", val));
					if(values != null && !values.isJsonNull()) {
						JsonArray valuesArray = values.getAsJsonArray();
						boolean valueFound = false;
						for(JsonElement element : valuesArray) {
							if(obj.get(key).equals(element)) {
								valueFound = true;
								break;
							}
						}
						if(!valueFound) {
							logFailure("Unable to find claim with value", args("key", key, "value", valuesArray));
							isPresent = false;
						}
					}

				} else {
					if(isEssential) {
						isPresent = false;
						logFailure("obj does NOT contain the claim ", args("key", key, "value", val));
					} else {
						log("obj does NOT contain the claim ", args("key", key, "value", val));
					}
				}
			}
		}
		return isPresent;
	}


	boolean checkScopeClaimsInObject(JsonObject obj, String scope) {
		return checkClaimsInObject(obj, getScopeClaims(scope, true));
	}


}
