package com.rewoo.elastic.connection;

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

import com.rewoo.elastic.constant.Constants;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpClientUtilsTest {
    private JsonObject validConfig;

    @Before
    public void setUp() {
        validConfig = createConfig("http://rewoo.com/rewoo");
    }

    @Test
    public void getScopeBaseUrl() {
        assertEquals(getBasePath(),
                HttpClientUtils.getScopeBaseUrl(createConfig("http://rewoo.com/rewoo")));
    }

    @Test
    public void createInitializedUriBuilder() throws URISyntaxException {
        Map<String, String> params = new HashMap<>();
        params.put("key1", "value1");
        URIBuilder builder = HttpClientUtils.createInitializedURIBuilder(Constants.GET_NODE_TYPES_METHOD, validConfig, params);
        URI uri = builder.build();
        assertEquals(getBasePath() + Constants.GET_NODE_TYPES_METHOD + "?key1=value1", uri.toString());
    }

    @Test
    public void login() {
        HttpClientUtils.httpClientSupplier = () -> {
            Map<String, Function<HttpUriRequest, Boolean>> validators = new HashMap<>();
            validators.put(getBasePath() + Constants.LOGIN_METHOD, uriRequest -> {
                return (uriRequest.getURI().toString().contains(Constants.SCOPE_USERNAME_REQUEST_KEY + "=" + "homer") &&
                        uriRequest.getURI().toString().contains(Constants.SCOPE_PASSWORD_REQUEST_KEY + "=" + "secret"));
            });
            return new HttpClientMock(validators, true);
        };
        HttpClientUtils.login(validConfig, HttpClientUtils.httpGetBuilder);
    }

    @Test
    public void logout() {
        HttpClientUtils.httpClientSupplier = () -> {
            Map<String, Function<HttpUriRequest, Boolean>> validators = new HashMap<>();
            validators.put(getBasePath() + Constants.LOGOUT_METHOD, uriRequest -> {
                return (uriRequest.getURI().toString().contains(Constants.SCOPE_SESSION_ID_REQUEST_KEY + "=" + "abcdefg42"));
            });
            return new HttpClientMock(validators, true);
        };
        HttpClientUtils.logout(validConfig, "abcdefg42", HttpClientUtils.httpGetBuilder);
    }

    @Test
    public void getSingleEmpty() {
        HttpClientUtils.httpClientSupplier = () -> {
            Map<String, Function<HttpUriRequest, String>> results = new HashMap<>();
            results.put(getBasePath() + Constants.LOGIN_METHOD, uriRequest -> {
                return "{ \"success\": true, \"" + Constants.SCOPE_SESSION_ID_RESPONSE_KEY + "\": \"abcdefg42\", \"message\": \"\" }";
            });
            results.put(getBasePath() + Constants.GET_NODE_TYPES_METHOD, uriRequest -> {
                return "{ \"success\": true, \"nodeTypes\": [], \"message\": \"\" }";
            });
            return new HttpClientMock(Collections.emptyMap(), results , false);
        };
        JsonObject result = HttpClientUtils.getSingle(Constants.GET_NODE_TYPES_METHOD, validConfig, new HashMap<>());
        assertTrue(result.getBoolean("success"));
        assertEquals(0, result.getString("message").length());
        assertEquals(0, result.getJsonArray("nodeTypes").size());
    }

    private static String getBasePath() {
        return "http://rewoo.com/rewoo/" + Constants.API_SERVICE_NAME + "/";
    }

    private static JsonObject createConfig(String baseUrl) {
        return Json.createObjectBuilder()
                .add(Constants.SCOPE_INSTANCE_CONFIG_KEY, baseUrl)
                .add(Constants.SCOPE_USERNAME_CONFIG_KEY, "homer")
                .add(Constants.SCOPE_PASSWORD_CONFIG_KEY, "secret")
                .build();
    }
}
