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
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONObject;

public interface BetterHttpRequest {

    /**
     * May be used to modify the {@linkplain HttpUriRequest request} just
     * before it is being executed.
     * 
     * @see {@link HttpRequestBuilder#customize(RequestCustomizer)}
     */
    public static interface RequestCustomizer {
        /**
         * Customizes the request before the execution is done.
         * 
         * @param request the request to customize
         */
        void customize(final HttpUriRequest request);
    }
  
    /**
     * @return the HttpClient request object wrapped by this request
     */
    public HttpUriRequest unwrap();

    /**
     * @return the request URL
     */
    public String getRequestUrl();

    /**
     * Define the set of HTTP status codes which you anticipate to be returned by the server,
     * including error codes you'd like to explicitly handle. Any status code part of this set will
     * not be treated as an error, but returned to you as a normal server response. Any status codes
     * returned by the server that are <i>not</i> part of this set will be raised as an
     * {@link HttpResponseException}. This is very useful when dealing with REST-ful Web services,
     * where it is common to serve error stati that indicate a failure in the application logic
     * (e.g. 404 if a resource doesn't exist). You typically don't want to treat those as connection
     * errors, but gracefully handle them like a normal success code.
     * 
     * @param statusCodes
     *            the set of status codes that you want to manually handle as part of the response
     * @return this request
     */
    public BetterHttpRequest expecting(Integer... statusCodes);

    /**
     * Set maximum number of retries for this particular request.
     * 
     * @param retries
     *            the maximum number of retries should the request fail
     * @return this request
     */
    public BetterHttpRequest retries(int retries);

    /**
     * Set the socket timeout for this specific request.
     * 
     * @param timeout
     *            the timeout in milliseconds
     * @return this request
     */
    public BetterHttpRequest withTimeout(int timeout);


    /**
     * Sets the entity to send with this request.
     * 
     * @param entity the entity to set for this request
     * @throws UnsupportedOperationException
     *             if this request not supports entity modifications
     * @return this builder
     */
    public BetterHttpRequest entity(final HttpEntity entity);

    /**
     * Sets the json data to send with this request.
     * 
     * @param json the json data to send for this request
     * @throws UnsupportedOperationException
     *             if this request not supports entity modifications
     * @throws UnsupportedEncodingException
     * @return this builder
     */
    public BetterHttpRequest json(JSONObject json) throws UnsupportedEncodingException;
    
    /**
     * Appends data to send with this request.
     * 
     * @param data the data to append to this request
     * @throws UnsupportedOperationException
     *             if this request not supports data modifications
     * @return this builder
     */
    public BetterHttpRequest data(final NameValuePair ... data);

    /**
     * Appends a new {@link NameValuePair}, specified by the given
     * {@code name} and {@code value}, to this request.
     * 
     * @param name the name of the parameter to add to this request
     * @param value the value of the parameter to add to this request
     * @throws UnsupportedOperationException
     *             if this request not supports data modifications
     * @return this builder
     */
    public BetterHttpRequest data(final String name, final String value);
 
    /**
     * Appends the String representation of each key-value-pair of the given
     * map to this request.
     * 
     * @param data the {@link Map} containing the data to append to this request
     * @throws UnsupportedOperationException
     *             if this request not supports data modifications
     * @return this builder
     */
    public BetterHttpRequest data(final Map<?, ?> data);

    /**
     * Adds the given {@linkplain RequestCustomizer request customizer} to
     * this request. All customizers are being applied sequentially just
     * before the request is being executed.
     * 
     * @param customizer the customizer to add to this request
     * @return this builder
     */
    public BetterHttpRequest customize(final RequestCustomizer customizer);

    /**
     * Adds a header with the given {@code name} and {@code value}
     * to this request.
     * 
     * @param name
     * @param value
     * @return this builder
     */
    public BetterHttpRequest header(final String name, final String value);
    
    /**
     * Adds the given {@linkplain Header header} to this request.
     * 
     * @param header
     * @return this builder
     */
    public BetterHttpRequest header(final Header header);
    
    /**
     * Sets the encoding for this request.
     * 
     * @param charset 
     * @return this builder
     */
    public BetterHttpRequest charset(final String charset);

    /**
     * Sets the behavior of redirection following for this request. The
     * behavior effects for this request only.
     * 
     * @param follow {@code true}, if redirects should be followed, otherwise
     *               {@code false}
     * @return this builder
     */
    public BetterHttpRequest followRedirects(final boolean follow);
    
    /**
     * Sends the current request. This method uses a special retry-logic (on top of that employed by
     * HttpClient, which is better suited to handle network fail-overs when e.g. switching between
     * Wi-Fi and 3G).
     */
    public BetterHttpResponse send() throws IOException;
}
