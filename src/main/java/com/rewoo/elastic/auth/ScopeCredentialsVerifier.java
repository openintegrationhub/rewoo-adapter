/*
 *     Copyright 2019 REWOO Technologies AG
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.rewoo.elastic.auth;

import com.rewoo.elastic.connection.HttpClientUtils;
import com.rewoo.elastic.constant.Constants;
import io.elastic.api.InvalidCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
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
