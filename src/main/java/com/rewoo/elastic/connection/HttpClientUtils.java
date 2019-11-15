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

package com.rewoo.elastic.connection;

import com.rewoo.elastic.constant.Constants;
import io.elastic.api.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class HttpClientUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    static final Function<URIBuilder, ? extends HttpRequestBase> httpGetBuilder = uriBuilder ->  {
            try {
                return new HttpGet(uriBuilder.build());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
    };


    private static final Function<URIBuilder, ? extends HttpRequestBase> httpPostBuilder = uriBuilder -> {
            try {
                return new HttpPost(uriBuilder.build());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
    };

    static Supplier<CloseableHttpClient> httpClientSupplier = HttpClients::createDefault;


    @SuppressWarnings("WeakerAccess")
    public static JsonObject getSingle(final String path,
                                       final JsonObject config,
                                       final Map<String, String> params) {
        return JSON.parseObject(executeAuthenticated(path, params, config, null, httpGetBuilder));
    }

    @SuppressWarnings("WeakerAccess")
    public static JsonArray getMany(final String path,
                                    final JsonObject config,
                                    final Map<String, String> params) {
        return JSON.parseArray(executeAuthenticated(path, params, config, null, httpGetBuilder));
    }

    @SuppressWarnings("WeakerAccess")
    public static JsonObject post(final String path,
                                  final JsonObject config,
                                  final JsonObject body,
                                  final Map<String, String> params) {
        return JSON.parseObject(executeAuthenticated(path, params, config, body, httpPostBuilder));
    }

    static JsonObject login(final JsonObject config, Function<URIBuilder, ? extends HttpRequestBase> requestBuilder) {
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put(Constants.SCOPE_USERNAME_REQUEST_KEY, config.getJsonString(Constants.SCOPE_USERNAME_CONFIG_KEY).getString());
        loginParams.put(Constants.SCOPE_PASSWORD_REQUEST_KEY, config.getJsonString(Constants.SCOPE_PASSWORD_CONFIG_KEY).getString());

        URIBuilder uriBuilder = createInitializedURIBuilder(Constants.LOGIN_METHOD, config, loginParams);
        HttpRequestBase request = requestBuilder.apply(uriBuilder);
        logger.info("Try to login via {}", request.toString());
        String loginAnswer;
        try {
            loginAnswer = sendRequest(request);
        } catch(Exception e) {
            logger.error("Error while performing login.", e);
            throw e;
        }
        return JSON.parseObject(loginAnswer);
    }

    @SuppressWarnings("UnusedReturnValue")
    static JsonObject logout(final JsonObject config, final String sessionId, Function<URIBuilder, ? extends HttpRequestBase> requestBuilder) {
        Map<String, String> logoutParams = new HashMap<>();
        logoutParams.put(Constants.SCOPE_SESSION_ID_REQUEST_KEY, sessionId);

        URIBuilder uriBuilder = createInitializedURIBuilder(Constants.LOGOUT_METHOD, config, logoutParams);
        HttpRequestBase request = requestBuilder.apply(uriBuilder);
        logger.info("Try to logout user");
        String logoutAnswer;
        try {
            logoutAnswer = sendRequest(request);
        } catch(Exception e) {
            logger.error("Error while performing logout.", e);
            throw e;
        }
        return JSON.parseObject(logoutAnswer);
    }

    static String getScopeBaseUrl(final JsonObject configuration) {
        String scopeBaseUrl = configuration.getString(Constants.SCOPE_INSTANCE_CONFIG_KEY);
        if (scopeBaseUrl == null) {
            throw new IllegalStateException("You need to set a scope instance url");
        }
        if (!scopeBaseUrl.endsWith("/")) {
            scopeBaseUrl += "/";
        }
        scopeBaseUrl += Constants.API_SERVICE_NAME + "/";
        return scopeBaseUrl;
    }

    static URIBuilder createInitializedURIBuilder(final String methodToCall, final JsonObject configuration, Map<String, String> params) {
        try {
            URIBuilder uriBuilder = new URIBuilder(getScopeBaseUrl(configuration) + methodToCall);
            for (Map.Entry<String, String> param : params.entrySet()) {
                uriBuilder.addParameter(param.getKey(), param.getValue());
            }
            return uriBuilder;
        } catch(Exception e) {
            throw new IllegalArgumentException("Unable to create valid URI for " + methodToCall, e);
        }
    }

    /**
     * Authenticates the current user, executes the method defined by the path-param and finally logs out the user again.
     * @param path The method to call (e.g. "findNode")
     * @param params Additional parameters for the method call
     * @param config The config of the workflow
     * @param body When using a requestBuilder creating a POST, PUT or PATCH request you can specify some payload for the request body
     * @param requestBuilder Factory for creating a suitable {@link HttpRequestBase} object used for the request
     * @return The answer of the call as a JSON string
     */
    private static String executeAuthenticated(final String path,
                                               final Map<String, String> params, final JsonObject config,
                                               final JsonObject body,
                                               Function<URIBuilder, ? extends HttpRequestBase> requestBuilder) {
        JsonObject loginAnswer = login(config, requestBuilder);
        String actualRequestAnswer;
        final String currentSessionId = loginAnswer.getString(Constants.SCOPE_SESSION_ID_RESPONSE_KEY);
        HttpRequestBase request = null;
        try {
            params.put(Constants.SCOPE_SESSION_ID_REQUEST_KEY, currentSessionId);
            URIBuilder uriBuilder = createInitializedURIBuilder(path, config, params);
            request = requestBuilder.apply(uriBuilder);
            logger.info("Try to call method {}", request.toString());
            if (body != null) {
                if (!(request instanceof HttpPost)) {
                    throw new IllegalArgumentException("If you want to pass data via the request body you must use an HTTP POST, PUT or PATCH request!");
                }
                ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(JSON.stringify(body)));
            }
            actualRequestAnswer = sendRequest(request);
        } catch(Exception e) {
            logger.error("Error while performing request " + (request == null ? path : request.toString()), e);
            throw new RuntimeException(e);
        } finally {
            if (currentSessionId != null) {
                logout(config, currentSessionId, requestBuilder);
            }
        }
        return actualRequestAnswer;
    }

    private static String sendRequest(final HttpRequestBase request) {
        request.addHeader(HTTP.CONTENT_TYPE, "application/json");

        final CloseableHttpClient httpClient = httpClientSupplier.get();
        try {
            final CloseableHttpResponse response = httpClient.execute(request);
            final HttpEntity responseEntity = response.getEntity();
            final StatusLine statusLine = response.getStatusLine();
            final int statusCode = statusLine.getStatusCode();
            logger.info("Got {} response", statusCode);
            if (responseEntity == null) {
                throw new RuntimeException("Null response received");
            }
            final String result = EntityUtils.toString(responseEntity);
            if (statusCode > 202) {
                throw new RuntimeException(result);
            }
            EntityUtils.consume(responseEntity);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error("Failed to close HttpClient", e);
            }
        }
    }
}


