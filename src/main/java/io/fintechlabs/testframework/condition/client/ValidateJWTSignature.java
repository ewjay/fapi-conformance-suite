package io.fintechlabs.testframework.condition.client;

import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.JWSVerifierFactory;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jwt.SignedJWT;
import io.fintechlabs.testframework.condition.AbstractCondition;
import io.fintechlabs.testframework.condition.PostEnvironment;
import io.fintechlabs.testframework.condition.PreEnvironment;
import io.fintechlabs.testframework.logging.TestInstanceEventLog;
import io.fintechlabs.testframework.testmodule.Environment;

import java.security.Key;
import java.text.ParseException;
import java.util.List;

/**
 * @author jricher
 *
 */
public class ValidateJWTSignature extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 */
	public ValidateJWTSignature(String testId, TestInstanceEventLog log, ConditionResult conditionResultOnFailure, String... requirements) {
		super(testId, log, conditionResultOnFailure, requirements);
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = { "jwt_token", "jwt_jwks" })
	@PostEnvironment(strings = "jwt_token_signing_alg")
	public Environment evaluate(Environment env) {

		if (!env.containsObject("jwt_token")) {
			throw error("Couldn't find parsed JWT token");
		}

		if (!env.containsObject("jwt_jwks")) {
			throw error("Couldn't find public keys for JWT token");
		}

		String idToken = env.getString("jwt_token", "value");
		JsonObject serverJwks = env.getObject("jwt_jwks"); // to validate the signature

		try {
			// translate stored items into nimbus objects
			SignedJWT jwt = SignedJWT.parse(idToken);
			JWKSet jwkSet = JWKSet.parse(serverJwks.toString());

			SecurityContext context = new SimpleSecurityContext();

			JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);

			JWSKeySelector<SecurityContext> selector = new JWSVerificationKeySelector<>(jwt.getHeader().getAlgorithm(), jwkSource);

			List<? extends Key> keys = selector.selectJWSKeys(jwt.getHeader(), context);
			for (Key key : keys) {
				JWSVerifierFactory factory = new DefaultJWSVerifierFactory();
				JWSVerifier verifier = factory.createJWSVerifier(jwt.getHeader(), key);

				if (jwt.verify(verifier)) {
					String alg = jwt.getHeader().getAlgorithm().getName();
					env.putString("jwt_token_signing_alg", alg);
					logSuccess("JWT Token signature validated", args("algorithm", alg));
					return env;
				} else {
					// failed to verify with this key, moving on
					// not a failure yet as it might pass a different key
				}
			}

			// if we got here, it hasn't been verified on any key
			throw error("Unable to verify JWT token signature based on server keys");

		} catch (JOSEException | ParseException e) {
			throw error("Error validating JWT Token signature", e);
		}

	}

}
