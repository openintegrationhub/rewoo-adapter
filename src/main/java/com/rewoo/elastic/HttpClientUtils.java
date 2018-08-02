package com.rewoo.elastic;

import io.elastic.api.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public final class HttpClientUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    public static JsonObject getSingle(final String path,
                                    final JsonObject configuration) {
        return JSON.parseObject(get(path, configuration));
    }

    public static JsonArray getMany(final String path,
                                    final JsonObject configuration) {
        return JSON.parseArray(get(path, configuration));
    }

    private static String get(final String path,
                              final JsonObject configuration) {
        final String requestURI = getScopeBaseUrl(configuration) + path;
        final HttpGet httpGet = new HttpGet(requestURI);
        return executeAuthenticated(httpGet, configuration);
    }

    public static JsonObject post(final String path,
                                  final JsonObject configuration,
                                  final JsonObject body) {
        final String requestURI = getScopeBaseUrl(configuration) + path;
        final HttpPost httpPost = new HttpPost(requestURI);
        try {
            httpPost.setEntity(new StringEntity(JSON.stringify(body)));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        final String response = executeAuthenticated(httpPost, configuration);
        return JSON.parseObject(response);
    }

    private static String executeAuthenticated(final HttpRequestBase request,
                                               final JsonObject configuration) {
        JsonObject loginAnswer = login(configuration);
        String actualRequestAnswer = null;
        final String currentSessionId = loginAnswer.getString(Constants.SCOPE_SESSION_ID_RESPONSE_KEY);
        try {
            actualRequestAnswer = sendRequest(request, configuration,
                    (req, conf) -> req.addHeader(new BasicHeader(Constants.SCOPE_SESSION_ID_REQUEST_KEY, currentSessionId)));
        } catch(Exception e) {
            logger.error("Error while performing request " + request.toString(), e);
            throw e;
        } finally {
            if (currentSessionId != null) {
                logout(configuration, currentSessionId);
            }
        }
        return actualRequestAnswer;
    }

    private static JsonObject login(final JsonObject configuration) {
        HttpGet request = new HttpGet(getScopeBaseUrl(configuration) + Constants.LOGIN_METHOD);
        logger.info("Try to login via {}", request.toString());
        String loginAnswer;
        try {
            loginAnswer = sendRequest(request, configuration, (req, conf) -> {
            req.addHeader(new BasicHeader(Constants.SCOPE_USERNAME_REQUEST_KEY, conf.getJsonString(Constants.SCOPE_USERNAME_CONFIG_KEY).getString()));
            req.addHeader(new BasicHeader(Constants.SCOPE_PASSWORD_REQUEST_KEY, conf.getJsonString(Constants.SCOPE_PASSWORD_CONFIG_KEY).getString()));
        });
        } catch(Exception e) {
            logger.error("Error while performing login.", e);
            throw e;
        }
        return JSON.parseObject(loginAnswer);
    }

    @SuppressWarnings("UnusedReturnValue")
    private static JsonObject logout(final JsonObject configuration, final String sessionId) {
        String logoutAnswer;
        try {
            logoutAnswer = sendRequest(new HttpGet(getScopeBaseUrl(configuration) + Constants.LOGIN_METHOD), configuration,
                    (req, conf) -> req.addHeader(new BasicHeader(Constants.SCOPE_SESSION_ID_REQUEST_KEY, sessionId)));
        } catch(Exception e) {
            logger.error("Error while performing logout.", e);
            throw e;
        }
        return JSON.parseObject(logoutAnswer);
    }

    private static String sendRequest(final HttpRequestBase request,
                                            final JsonObject configuration,
                                            final AdaptHttpHeader headerAdapter) {

        request.addHeader(HTTP.CONTENT_TYPE, "application/json");
        headerAdapter.adapt(request, configuration);
        final CloseableHttpClient httpClient = HttpClients.createDefault();
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

    private static String getScopeBaseUrl(final JsonObject configuration) {
        String scopeBaseUrl = configuration.getString(com.rewoo.elastic.Constants.SCOPE_INSTANCE_CONFIG_KEY);
        if (scopeBaseUrl == null) {
            throw new IllegalStateException("You need to set a scope instance url");
        }
        if (!scopeBaseUrl.endsWith("/")) {
            scopeBaseUrl += "/";
        }
        return scopeBaseUrl;
    }

    @FunctionalInterface
    private interface AdaptHttpHeader  {
        void adapt(final HttpRequestBase request, final JsonObject configuration);
    }

}


