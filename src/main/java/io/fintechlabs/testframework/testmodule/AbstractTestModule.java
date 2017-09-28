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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

import com.google.gson.JsonObject;

import io.fintechlabs.testframework.condition.Condition;
import io.fintechlabs.testframework.condition.ConditionError;
import io.fintechlabs.testframework.frontChannel.BrowserControl;
import io.fintechlabs.testframework.logging.EventLog;

/**
 * @author jricher
 *
 */
public abstract class AbstractTestModule implements TestModule {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractTestModule.class);

	private String name;
	protected String id = null; // unique identifier for the test, set from the outside
	protected Status status = Status.UNKNOWN; // current status of the test
	protected Result result = Result.UNKNOWN; // results of running the test
	protected EventLog eventLog;
	protected List<TestModuleEventListener> listeners = new ArrayList<>();
	protected BrowserControl browser;
	protected Map<String, String> exposed = new HashMap<>(); // exposes runtime values to outside modules
	protected Environment env = new Environment(); // keeps track of values at runtime

	/**
	 * @param name
	 */
	public AbstractTestModule(String name) {
		this.name = name;
	}

	/**
	 * Create and evaluate a Condition in the current environment. Throw a @TestFailureException if the Condition fails.
	 */
	protected void require(Class<? extends Condition> conditionClass) {
		try {
			
			// create a new condition object from the class above
			Condition condition = conditionClass
				.getDeclaredConstructor(String.class, EventLog.class)
				.newInstance(id, eventLog);
	
			// evaluate the condition and assign its results back to our environment
			env = condition.evaluate(env);
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			logger.error("Couldn't create required condition object", e);
			fireTestFailure();
			throw new TestFailureException(getId(), "Couldn't create required condition: " + conditionClass.getSimpleName());
		} catch (ConditionError error) {
			logger.info("Test condition failure: " + error.getMessage());
			fireTestFailure();
			throw new TestFailureException(error);
		}
	}

	/**
	 * Create and evaluate a Condition in the current environment. Log but ignore if the Condition fails.
	 */
	protected void optional(Class<? extends Condition> conditionClass) {
		try {
			
			// create a new condition object from the class above
			Condition condition = conditionClass
				.getDeclaredConstructor(String.class, EventLog.class)
				.newInstance(id, eventLog);
	
			// evaluate the condition and assign its results back to our environment
			env = condition.evaluate(env);
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			logger.error("Couldn't create optional condition object", e);
		} catch (ConditionError error) {
			logger.info("Ignoring optional condition failure: " + error.getMessage());
		}
	}

	/**
	 * Create and evaluate a Condition in the current environment. Throw a @TestFailureException if the Condition succeeds. This is the inverse of require().
	 */
	protected void expectFailure(Class<? extends Condition> conditionClass) {
		try {
			
			// create a new condition object from the class above
			Condition condition = conditionClass
				.getDeclaredConstructor(String.class, EventLog.class)
				.newInstance(id, eventLog);
	
			// evaluate the condition and assign its results back to our environment
			env = condition.evaluate(env);
			
			// if we got here, the condition succeeded but we're expecting a failure so throw an error
			fireTestFailure();
			throw new TestFailureException(getId(), "Condition failure expected, but got success: " + conditionClass.getSimpleName());
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			logger.error("Couldn't create required condition object", e);
			fireTestFailure();
			throw new TestFailureException(getId(), "Couldn't create required condition: " + conditionClass.getSimpleName());
		} catch (ConditionError error) {
			logger.info("Test condition failure as expected: " + error.getMessage());
		}
	}

	public String getId() {
		return id;
	}

	public Status getStatus() {
		return status;		
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	@Override
	public boolean addListener(TestModuleEventListener e) {
		return listeners.add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#remove(java.lang.Object)
	 */
	@Override
	public boolean removeListener(TestModuleEventListener o) {
		return listeners.remove(o);
	}

	protected void logFinalEnv() {
		Map<String, Object> finalEnv = new HashMap<>();
		for (String key : env.allObjectIds()) {
			finalEnv.put(key, env.get(key));
		}
		
		eventLog.log(getId(), "final_env", finalEnv);
	}

	protected void fireSetupDone() {
		for (TestModuleEventListener listener : listeners) {
			listener.setupDone();
		}
		
		eventLog.log(getId(), getName(), "Setup Done");
	
	}

	protected void fireTestSuccess() {
		setResult(Result.PASSED);
		for (TestModuleEventListener listener : listeners) {
			listener.testSuccess();
		}
		eventLog.log(getId(), getName(), "Success");
	
		logFinalEnv();
	}

	private void fireTestFailure() {
		setResult(Result.FAILED);
		for (TestModuleEventListener listener : listeners) {
			listener.testFailure();
		}
		eventLog.log(getId(), getName(), "Failure");
	
		logFinalEnv();
	}

	protected void fireInterrupted() {
		for (TestModuleEventListener listener : listeners) {
			listener.interrupted();
		}
		eventLog.log(getId(), getName(), "Interrupted");
	
		logFinalEnv();
	}

	/**
	 * utility function to convert an incoming multi-value map to a JSonObject for storage
	 * @param params
	 * @return
	 */
	protected JsonObject mapToJsonObject(MultiValueMap<String, String> params) {
		JsonObject o = new JsonObject();
		for (String key : params.keySet()) {
			o.addProperty(key, params.getFirst(key));
		}
		return o;
	}

	/**
	 * @return the result
	 */
	public Result getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	private void setResult(Result result) {
		this.result = result;
	}

	private void expose(String key, String val) {
		exposed.put(key, val);
	}

	protected void exposeEnvString(String key) {
		String val = env.getString(key);
		expose(key, val);
	}

	@Override
	public Map<String, String> getExposedValues() {
		return exposed;
	}

	@Override
	public BrowserControl getBrowser() {
		return this.browser;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

}