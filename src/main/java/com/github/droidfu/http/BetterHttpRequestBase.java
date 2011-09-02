/* Copyright (c) 2009 Matthias Kaeppler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.droidfu.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import android.util.Log;

import com.github.droidfu.cachefu.HttpResponseCache;
import com.github.droidfu.http.CachedHttpResponse.ResponseData;

public abstract class BetterHttpRequestBase implements BetterHttpRequest,
        ResponseHandler<BetterHttpResponse> {

    private static final int MAX_RETRIES = 5;

    protected static final String HTTP_CONTENT_TYPE_HEADER = "Content-Type";

    protected List<Integer> expectedStatusCodes = new ArrayList<Integer>();

    protected AbstractHttpClient httpClient;

    protected HttpUriRequest request;

    protected int maxRetries = MAX_RETRIES;

    private int oldTimeout; // used to cache the global timeout when changing it for one request

    private int executionCount;
    
    protected List<Header> headers;
    
    protected List<RequestCustomizer> customizers;
    
    protected String charset = null;
    
    protected Boolean followRedirects;
    

    BetterHttpRequestBase(AbstractHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public HttpUriRequest unwrap() {
        return request;
    }

    public String getRequestUrl() {
        return request.getURI().toString();
    }

    public BetterHttpRequestBase expecting(Integer... statusCodes) {
        expectedStatusCodes = Arrays.asList(statusCodes);
        return this;
    }

    public BetterHttpRequestBase retries(int retries) {
        if (retries < 0) {
            this.maxRetries = 0;
        } else if (retries > MAX_RETRIES) {
            this.maxRetries = MAX_RETRIES;
        } else {
            this.maxRetries = retries;
        }
        return this;
    }

    public BetterHttpRequest withTimeout(int timeout) {
        oldTimeout = httpClient.getParams().getIntParameter(CoreConnectionPNames.SO_TIMEOUT,
                BetterHttp.DEFAULT_SOCKET_TIMEOUT);
        BetterHttp.setSocketTimeout(timeout);
        return this;
    }

    public BetterHttpResponse send() throws IOException {
        prepareRequest();
      
        BetterHttpRequestRetryHandler retryHandler = new BetterHttpRequestRetryHandler(maxRetries);
        // tell HttpClient to user our own retry handler
        httpClient.setHttpRequestRetryHandler(retryHandler);

        HttpContext context = new BasicHttpContext();

        // Grab a coffee now and lean back, I'm not good at explaining stuff. This code realizes
        // a second retry layer on top of HttpClient. Rationale: HttpClient.execute sometimes craps
        // out even *before* the HttpRequestRetryHandler set above is called, e.g. on a
        // "Network unreachable" SocketException, which can happen when failing over from Wi-Fi to
        // 3G or vice versa. Hence, we catch these exceptions, feed it through the same retry
        // decision method *again*, and align the execution count along the way.
        boolean retry = true;
        IOException cause = null;
        while (retry) {
            try {
                Log.d(BetterHttp.LOG_TAG, "Sending HTTP request to " + request.getURI());
                return httpClient.execute(request, this, context);
            } catch (IOException e) {
                cause = e;
                retry = retryRequest(retryHandler, cause, context);
            } catch (NullPointerException e) {
                // there's a bug in HttpClient 4.0.x that on some occasions causes
                // DefaultRequestExecutor to throw an NPE, see
                // http://code.google.com/p/android/issues/detail?id=5255
                cause = new IOException("NPE in HttpClient" + e.getMessage());
                retry = retryRequest(retryHandler, cause, context);
            } finally {
                // if timeout was changed with this request using withTimeout(), reset it
                if (oldTimeout != BetterHttp.getSocketTimeout()) {
                    BetterHttp.setSocketTimeout(oldTimeout);
                }
            }
        }

        // no retries left, crap out with exception
        ConnectException ex = new ConnectException();
        ex.initCause(cause);
        throw ex;
    }

    private boolean retryRequest(BetterHttpRequestRetryHandler retryHandler, IOException cause,
            HttpContext context) {
        Log.e(BetterHttp.LOG_TAG, "Intercepting exception that wasn't handled by HttpClient");
        executionCount = Math.max(executionCount, retryHandler.getTimesRetried());
        return retryHandler.retryRequest(cause, ++executionCount, context);
    }

    public BetterHttpResponse handleResponse(HttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        if (expectedStatusCodes != null && !expectedStatusCodes.isEmpty()) {
          if (!expectedStatusCodes.contains(status)) {
            throw new HttpResponseException(status, "Unexpected status code: " + status);
          }
        } else if (status >= 400) { // TODO - Make this configurable & compatible with the old default droid-fu behavior
          throw new HttpResponseException(status, "Error status code: " + status);
        }

        BetterHttpResponse bhttpr = new BetterHttpResponseImpl(response);
        HttpResponseCache responseCache = BetterHttp.getResponseCache();
        if (responseCache != null) {
            ResponseData responseData = new ResponseData(status, bhttpr.getResponseBodyAsBytes());
            responseCache.put(getRequestUrl(), responseData);
        }
        return bhttpr;
    }
    
    public BetterHttpRequest entity(final HttpEntity entity) {
        throw new UnsupportedOperationException(
                "This HTTP-method doesn't support to add an entity.");
    }

    public BetterHttpRequest json(JSONObject json) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException(
                "This HTTP-method doesn't support to add an entity.");
    }
    
    public BetterHttpRequest data(final NameValuePair ... data) {
        throw new UnsupportedOperationException(
                "This HTTP-method doesn't support to add data.");
    }

    public BetterHttpRequest data(final String name, final String value) {
        throw new UnsupportedOperationException(
                "This HTTP-method doesn't support to add data.");
    }

    public BetterHttpRequest data(final Map<?, ?> data) {
        throw new UnsupportedOperationException(
                "This HTTP-method doesn't support to add data.");
    }
    
    public BetterHttpRequest customize(final RequestCustomizer customizer) {
      getCustomizers().add(customizer);
      return this;
    }

    public BetterHttpRequest header(final String name, final String value) {
      getHeaders().add(new BasicHeader(name, value));
      return this;
    }
    
    public BetterHttpRequest header(final Header header) {
      getHeaders().add(header);
      return this;
    }

    public BetterHttpRequest charset(final String charset) {
      this.charset = charset;
      return this;
    }

    public BetterHttpRequest followRedirects(final boolean follow) {
      followRedirects = follow;
      return this;
    }
    
    protected List<Header> getHeaders() {
      if (headers == null) {
        headers = new ArrayList<Header>();
      }
      return headers;
    }
    
    protected List<RequestCustomizer> getCustomizers() {
      if (customizers == null) {
        customizers = new ArrayList<RequestCustomizer>();
      }
      return customizers;
    }
    
    protected AbstractHttpClient getClient() {
      return httpClient;
    }

    protected void prepareRequest() throws IOException {
      applyHeaders(request);
      if (followRedirects != null) {
        HttpClientParams.setRedirecting(request.getParams(), followRedirects);
      }
      applyCustomizers(request);
    }
    
    private void applyHeaders(final HttpRequest request) {
      if (headers != null) {
        for (final Header h : headers) {
          request.setHeader(h);
        }
      }
    }
    
    private void applyCustomizers(final HttpUriRequest request) {
      if (customizers != null) {
        for (final RequestCustomizer modifier : customizers) {
          modifier.customize(request);
        }
      }
    }
    
}
