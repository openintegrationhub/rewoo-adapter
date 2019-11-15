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

import static org.junit.Assert.assertTrue;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;


public class HttpClientMock extends CloseableHttpClient {
    private Map<String, Function<HttpUriRequest, Boolean>> validators;
    private Map<String, Function<HttpUriRequest, String>> results;
    private boolean strict;
    private HttpUriRequest uriRequest;

    HttpClientMock(Map<String, Function<HttpUriRequest, Boolean>> validators,
                          Map<String, Function<HttpUriRequest, String>> results,
                          boolean strict) {
        this.validators = validators;
        this.results = results;
        this.strict = strict;
    }

    HttpClientMock(Map<String, Function<HttpUriRequest, Boolean>> validators, boolean strict) {
        this(validators, Collections.emptyMap(), strict);
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) throws IOException, ClientProtocolException {
        uriRequest = ((HttpUriRequest) httpRequest);
        String baseUri = getBaseUri(uriRequest);
        if (strict) {
            assertTrue("Strict mode specified but found no validator for URI " + baseUri, validators.containsKey(baseUri));
        }
        Function<HttpUriRequest, Boolean> validator = validators.get(baseUri);
        if (validator != null) {
            assertTrue(validators.get(baseUri).apply(uriRequest));
        }
        return new CloseableResponse();
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return null;
    }

    private static String getBaseUri(HttpUriRequest uriRequest) {
        return uriRequest.getURI().toString().split("\\?")[0];
    }


    private final class CloseableResponse implements CloseableHttpResponse {
        @Override
        public void close() throws IOException {

        }

        @Override
        public StatusLine getStatusLine() {
            return new BasicStatusLine(new ProtocolVersion("http", 1, 1), 200, "OK");
        }

        @Override
        public void setStatusLine(StatusLine statusLine) {

        }

        @Override
        public void setStatusLine(ProtocolVersion protocolVersion, int i) {

        }

        @Override
        public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {

        }

        @Override
        public void setStatusCode(int i) throws IllegalStateException {

        }

        @Override
        public void setReasonPhrase(String s) throws IllegalStateException {

        }

        @Override
        public HttpEntity getEntity() {
            try {
                if (uriRequest == null || !results.containsKey(getBaseUri(uriRequest))) {
                    return new StringEntity("{}");
                }
                return new StringEntity(results.get(getBaseUri(uriRequest)).apply(uriRequest));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public void setEntity(HttpEntity httpEntity) {

        }

        @Override
        public Locale getLocale() {
            return null;
        }

        @Override
        public void setLocale(Locale locale) {

        }

        @Override
        public ProtocolVersion getProtocolVersion() {
            return null;
        }

        @Override
        public boolean containsHeader(String s) {
            return false;
        }

        @Override
        public Header[] getHeaders(String s) {
            return new Header[0];
        }

        @Override
        public Header getFirstHeader(String s) {
            return null;
        }

        @Override
        public Header getLastHeader(String s) {
            return null;
        }

        @Override
        public Header[] getAllHeaders() {
            return new Header[0];
        }

        @Override
        public void addHeader(Header header) {

        }

        @Override
        public void addHeader(String s, String s1) {

        }

        @Override
        public void setHeader(Header header) {

        }

        @Override
        public void setHeader(String s, String s1) {

        }

        @Override
        public void setHeaders(Header[] headers) {

        }

        @Override
        public void removeHeader(Header header) {

        }

        @Override
        public void removeHeaders(String s) {

        }

        @Override
        public HeaderIterator headerIterator() {
            return null;
        }

        @Override
        public HeaderIterator headerIterator(String s) {
            return null;
        }

        @Override
        public HttpParams getParams() {
            return null;
        }

        @Override
        public void setParams(HttpParams httpParams) {

        }
    }
}
