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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

@RunWith(MockitoJUnitRunner.class)
public class AddBasicAuthClientSecretAuthenticationParameters_UnitTest {

	@Spy
	private Environment env = new Environment();

	@Mock
	private TestInstanceEventLog eventLog;

	private JsonObject client;

	private String expectedAuth;

	private AddBasicAuthClientSecretAuthenticationParameters cond;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		cond = new AddBasicAuthClientSecretAuthenticationParameters("UNIT-TEST", eventLog, ConditionResult.INFO);

		// Example values from RFC 6749

		client = new JsonParser().parse("{"
			+ "\"client_id\":\"s6BhdRkqt3\","
			+ "\"client_secret\":\"7Fjfp0ZBr1KtDRbnfVdmIw\""
			+ "}").getAsJsonObject();

		expectedAuth = "Basic czZCaGRSa3F0Mzo3RmpmcDBaQnIxS3REUmJuZlZkbUl3";

		env.putObject("client", client);
	}

	/**
	 * Test method for {@link io.fintechlabs.testframework.condition.client.AddBasicAuthClientSecretAuthenticationParameters#evaluate(io.fintechlabs.testframework.testmodule.Environment)}.
	 */
	@Test
	public void testEvaluate_noError() {

		cond.evaluate(env);

		verify(env, atLeastOnce()).getString("client", "client_id");
		verify(env, atLeastOnce()).getString("client", "client_secret");

		assertThat(env.getString("token_endpoint_request_headers", "Authorization")).isEqualTo(expectedAuth);
	}

}
