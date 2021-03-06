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

package io.fintechlabs.testframework.heart;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.JsonObject;

import io.fintechlabs.testframework.condition.Condition.ConditionResult;
import io.fintechlabs.testframework.condition.as.AddIntrospectionUrlToServerConfiguration;
import io.fintechlabs.testframework.condition.as.AddRevocationUrlToServerConfiguration;
import io.fintechlabs.testframework.condition.as.CopyAccessTokenFromASToClient;
import io.fintechlabs.testframework.condition.as.CreateIntrospectionResponse;
import io.fintechlabs.testframework.condition.as.EnsureResourceAssertionTypeIsJwt;
import io.fintechlabs.testframework.condition.as.ExtractAssertionFromIntrospectionRequest;
import io.fintechlabs.testframework.condition.as.ExtractJWKsFromResourceConfiguration;
import io.fintechlabs.testframework.condition.as.GenerateBearerAccessToken;
import io.fintechlabs.testframework.condition.as.GenerateServerConfiguration;
import io.fintechlabs.testframework.condition.as.GetStaticResourceConfiguration;
import io.fintechlabs.testframework.condition.as.LoadServerJWKs;
import io.fintechlabs.testframework.condition.as.ValidateResourceAssertionClaims;
import io.fintechlabs.testframework.condition.as.ValidateResourceAssertionSignature;
import io.fintechlabs.testframework.condition.client.CallProtectedResourceWithBearerToken;
import io.fintechlabs.testframework.condition.client.CheckHeartServerJwksFields;
import io.fintechlabs.testframework.condition.client.ExtractTLSTestValuesFromResourceConfiguration;
import io.fintechlabs.testframework.condition.client.GetStaticClientConfiguration;
import io.fintechlabs.testframework.condition.common.CheckForKeyIdInServerJWKs;
import io.fintechlabs.testframework.condition.common.CheckHeartServerConfiguration;
import io.fintechlabs.testframework.condition.common.CheckServerConfiguration;
import io.fintechlabs.testframework.condition.common.DisallowInsecureCipher;
import io.fintechlabs.testframework.condition.common.DisallowTLS10;
import io.fintechlabs.testframework.condition.common.DisallowTLS11;
import io.fintechlabs.testframework.condition.common.EnsureTLS12;
import io.fintechlabs.testframework.condition.common.SetTLSTestHostFromConfig;
import io.fintechlabs.testframework.testmodule.AbstractTestModule;
import io.fintechlabs.testframework.testmodule.PublishTestModule;
import io.fintechlabs.testframework.testmodule.TestFailureException;

/**
 * @author jricher
 *
 */
@PublishTestModule(
	testName = "heart-rs-plain-get",
	displayName = "HEART RS with plain GET request",
	profile = "HEART",
	configurationFields = {
		"server.jwks",
		"client.client_id",
		"resource.resource_id",
		"resource.jwks",
		"resource.scope",
		"tls.testHost",
		"tls.testPort",
		"resource.resourceUrl",
		"resource.resourceMethod"
	}
)
public class PlainRS extends AbstractTestModule {

	public static Logger logger = LoggerFactory.getLogger(PlainRS.class);

	@Override
	public void configure(JsonObject config, String baseUrl) {
		env.putString("base_url", baseUrl);
		env.putObject("config", config);

		callAndStopOnFailure(SetTLSTestHostFromConfig.class);
		callAndStopOnFailure(EnsureTLS12.class, "HEART-OAuth2-6");
		callAndContinueOnFailure(DisallowTLS10.class, "HEART-OAuth2-6");
		callAndContinueOnFailure(DisallowTLS11.class, "HEART-OAuth2-6");

		callAndStopOnFailure(GenerateServerConfiguration.class, "HEART-OAuth2-3.1.5");

		callAndStopOnFailure(AddIntrospectionUrlToServerConfiguration.class, "HEART-OAuth2-3.1.5");
		callAndStopOnFailure(AddRevocationUrlToServerConfiguration.class, "HEART-OAuth2-3.1.5");

		exposeEnvString("discoveryUrl");
		exposeEnvString("issuer");

		callAndStopOnFailure(CheckServerConfiguration.class);


		// make sure the server configuration passes some basic sanity checks
		callAndStopOnFailure(CheckHeartServerConfiguration.class, "HEART-OAuth2-3.1.5");

		// load the server's keys as needed
		callAndStopOnFailure(LoadServerJWKs.class);

		callAndStopOnFailure(CheckHeartServerJwksFields.class, "HEART-OAuth2-3.1.5");
		callAndStopOnFailure(CheckForKeyIdInServerJWKs.class, "OIDCC-10.1");

		// Set up the resource configuration
		callAndStopOnFailure(GetStaticResourceConfiguration.class);
		callAndStopOnFailure(ExtractTLSTestValuesFromResourceConfiguration.class);

		callAndStopOnFailure(ExtractJWKsFromResourceConfiguration.class, "HEART-OAuth2-2.1.5");

		exposeEnvString("resource_id");

		// get the client's ID
		callAndStopOnFailure(GetStaticClientConfiguration.class);

		exposeEnvString("client_id");

		setStatus(Status.CONFIGURED);
		fireSetupDone();
	}

	/* (non-Javadoc)
	 * @see io.bspk.selenium.TestModule#start()
	 */
	@Override
	public void start() {

		setStatus(Status.RUNNING);

		eventLog.startBlock("Resource Endpoint TLS test");
		env.mapKey("tls", "resource_endpoint_tls");
		callAndContinueOnFailure(EnsureTLS12.class, ConditionResult.FAILURE);
		callAndContinueOnFailure(DisallowTLS10.class, ConditionResult.FAILURE);
		callAndContinueOnFailure(DisallowTLS11.class, ConditionResult.FAILURE);
		callAndContinueOnFailure(DisallowInsecureCipher.class, ConditionResult.FAILURE);

		eventLog.endBlock();
		env.unmapKey("tls");

		// create an access token for the client to use
		callAndStopOnFailure(GenerateBearerAccessToken.class);

		exposeEnvString("access_token");

		callAndStopOnFailure(CopyAccessTokenFromASToClient.class);

		setStatus(Status.WAITING);
		callAndStopOnFailure(CallProtectedResourceWithBearerToken.class);
		setStatus(Status.RUNNING);

		fireTestSuccess();
		setStatus(Status.FINISHED);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.TestModule#handleHttp(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.http.HttpSession, org.springframework.util.MultiValueMap, org.springframework.ui.Model)
	 */
	@Override
	public Object handleHttp(String path, HttpServletRequest req, HttpServletResponse res, HttpSession session, JsonObject requestParts) {
		// dispatch based on the path
		if (path.equals("introspect")) {
			return handleIntrospection(requestParts);
		} else {
			throw new TestFailureException(getId(), "Got unexpected HTTP call to " + path);
		}

	}

	/**
	 * @param requestParts
	 * @return
	 */
	private Object handleIntrospection(JsonObject requestParts) {

		setStatus(Status.RUNNING);

		env.putObject("introspection_request", requestParts);

		callAndStopOnFailure(ExtractAssertionFromIntrospectionRequest.class);

		callAndStopOnFailure(EnsureResourceAssertionTypeIsJwt.class);

		callAndStopOnFailure(ValidateResourceAssertionClaims.class);

		callAndStopOnFailure(ValidateResourceAssertionSignature.class);

		callAndStopOnFailure(CreateIntrospectionResponse.class);

		setStatus(Status.WAITING);

		return new ResponseEntity<>(env.getObject("introspection_response"), HttpStatus.OK);

	}

}
