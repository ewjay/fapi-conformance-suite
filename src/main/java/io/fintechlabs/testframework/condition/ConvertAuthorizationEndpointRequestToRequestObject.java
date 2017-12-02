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

package io.fintechlabs.testframework.condition;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.fintechlabs.testframework.logging.EventLog;
import io.fintechlabs.testframework.testmodule.Environment;

public class ConvertAuthorizationEndpointRequestToRequestObject extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 * @param optional
	 */
	public ConvertAuthorizationEndpointRequestToRequestObject(String testId, EventLog log, boolean optional) {
		super(testId, log, optional);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	public Environment evaluate(Environment env) {

		JsonObject authorizationEndpointRequest = env.get("authorization_endpoint_request");

		if (authorizationEndpointRequest == null) {
			return error("Couldn't find authorization endpoint request");
		}

		JsonObject requestObjectClaims = new JsonObject();
		for (Map.Entry<String, JsonElement> entry : authorizationEndpointRequest.entrySet()) {
			requestObjectClaims.add(entry.getKey(), entry.getValue());
		}

		env.put("request_object_claims", requestObjectClaims);

		logSuccess("Created request object claims", args("request_object_claims", requestObjectClaims));

		return env;
	}

}