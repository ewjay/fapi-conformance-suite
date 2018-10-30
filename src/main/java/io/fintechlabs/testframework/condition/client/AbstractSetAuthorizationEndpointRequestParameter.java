package io.fintechlabs.testframework.condition.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

public abstract class AbstractSetAuthorizationEndpointRequestParameter extends AbstractCondition {
	public AbstractSetAuthorizationEndpointRequestParameter(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	public Environment setParameter(Environment env, String name, String value) {
		JsonObject authorizationEndpointRequest = env.getObject("authorization_endpoint_request");
		authorizationEndpointRequest.addProperty(name, value);
		logSuccess("Added "+name+" request", args("authorization_endpoint_request", authorizationEndpointRequest));
		return env;
	}

	public Environment addToParameter(Environment env, String name, String value) {
		JsonObject authorizationEndpointRequest = env.getObject("authorization_endpoint_request");
		if(authorizationEndpointRequest.has(name)) {
			JsonElement jsonElement = authorizationEndpointRequest.get(name);
			if(jsonElement.isJsonPrimitive()) {
				authorizationEndpointRequest.addProperty(name, jsonElement.getAsString() + " " + value);
			}
		} else {
			authorizationEndpointRequest.addProperty(name, value);
		}
		logSuccess("Added "+name+" request", args("authorization_endpoint_request", authorizationEndpointRequest));
		return env;
	}

	public Environment setParameter(Environment env, String name, long value) {
		JsonObject authorizationEndpointRequest = env.getObject("authorization_endpoint_request");
		authorizationEndpointRequest.addProperty(name, value);
		logSuccess("Added "+name+" request", args("authorization_endpoint_request", authorizationEndpointRequest));
		return env;
	}

	public Environment setParameter(Environment env, String name, boolean value) {
		JsonObject authorizationEndpointRequest = env.getObject("authorization_endpoint_request");
		authorizationEndpointRequest.addProperty(name, value);
		logSuccess("Added "+name+" request", args("authorization_endpoint_request", authorizationEndpointRequest));
		return env;
	}



}
