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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

import java.util.Set;

public class SetAuthorizationEndpointRequestParametersFromList extends AbstractSetAuthorizationEndpointRequestParameter {

	public SetAuthorizationEndpointRequestParametersFromList(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	@Override
	@PreEnvironment(required = {"authorization_endpoint_request", "authorization_endpoint_request_parameter_list"} )
	@PostEnvironment(required = "authorization_endpoint_request")
	public Environment evaluate(Environment env) {
		JsonObject authorizationEndpointRequest = env.getObject("authorization_endpoint_request_parameter_list");
		Set<String> keys = authorizationEndpointRequest.keySet();
		for(String key : keys) {
			JsonElement value = authorizationEndpointRequest.get(key);
			if(value.isJsonPrimitive()) {
				JsonPrimitive primitiveValue = value.getAsJsonPrimitive();
				if(primitiveValue.isString()) {
					env = setParameter(env, key, authorizationEndpointRequest.get(key).getAsString());
				} else if(primitiveValue.isNumber()) {
					env = setParameter(env, key, authorizationEndpointRequest.get(key).getAsLong());
				} else if(primitiveValue.isBoolean()) {
					env = setParameter(env, key, authorizationEndpointRequest.get(key).getAsBoolean());
				}

			} else {
				throw error("Unsuppoorted key value type "+key, authorizationEndpointRequest);
			}

		}
		return env;
	}

}
