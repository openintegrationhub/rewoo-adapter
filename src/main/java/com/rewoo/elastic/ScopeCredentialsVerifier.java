package com.rewoo.elastic;


import io.elastic.api.InvalidCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import java.util.Collections;
import java.util.HashMap;

/**
 * Implementation of {@link io.elastic.api.CredentialsVerifier} used to verify that credentials provide by user
 * are valid. This is accomplished by sending a login request to the REWOO Scope server API and calling a no-op.
 * In case of a successful response we assume credentials are valid. Otherwise invalid.
 */
@SuppressWarnings("unused")
public class ScopeCredentialsVerifier implements io.elastic.api.CredentialsVerifier {
    private static final Logger logger = LoggerFactory.getLogger(ScopeCredentialsVerifier.class);

    @Override
    public void verify(final JsonObject configuration) throws InvalidCredentialsException {
        logger.info("Verifying user credentials");
        try {
            final JsonObject user = HttpClientUtils.getSingle(Constants.CHECK_API_METHOD, configuration, new HashMap<>());
            logger.info("User {} successfully retrieved. Credentials are valid", configuration.getString(Constants.SCOPE_USERNAME_CONFIG_KEY));
        } catch (Exception e) {
            throw new InvalidCredentialsException("Failed to verify credentials", e);
        }
    }
}
