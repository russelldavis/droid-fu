/*
 * Copyright (c) 2009 Matthias K�ppler, Thomas Dudek, Russell Davis
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Some of this code is based on httpclient-fluent-builder.
 * See http://code.google.com/p/httpclient-fluent-builder/source/browse/trunk/src/de/mastacode/http/Http.java
 */

package com.github.droidfu.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

class BetterHttpRequestWithBody extends BetterHttpRequestBase {

  protected List<NameValuePair> data;
  protected HttpEntity entity;
  
  BetterHttpRequestWithBody(AbstractHttpClient httpClient, String url, HashMap<String, String> defaultHeaders) {
    super(httpClient);
    this.request = new HttpPost(url);
    for (String header : defaultHeaders.keySet()) {
      request.setHeader(header, defaultHeaders.get(header));
    }
  }

  BetterHttpRequestWithBody(AbstractHttpClient httpClient, String url, HttpEntity payload,
      HashMap<String, String> defaultHeaders) {
    super(httpClient);
    this.request = new HttpPost(url);
    ((HttpEntityEnclosingRequest) request).setEntity(payload);

    request.setHeader(HTTP_CONTENT_TYPE_HEADER, payload.getContentType().getValue());
    for (String header : defaultHeaders.keySet()) {
      request.setHeader(header, defaultHeaders.get(header));
    }
  }

  @Override public BetterHttpRequest entity(final HttpEntity entity) {
    ensureNoData();
    
    this.entity = entity;
    return this;
  }

  @Override public BetterHttpRequest json(final JSONObject json) throws UnsupportedEncodingException {
    ensureNoData();

    StringEntity entity = new StringEntity(json.toString());
    entity.setContentEncoding("UTF-8");
    entity.setContentType("application/json");
    this.entity = entity;
    
    return this;
  }
  
  @Override public BetterHttpRequest data(final String name, final String value) {
    ensureNoEntity();

    getData().add(new BasicNameValuePair(name, value));
    return this;
  }

  @Override public BetterHttpRequest data(final NameValuePair... data) {
    ensureNoEntity();

    if (data != null) {
      final List<NameValuePair> dataList = getData();
      for (final NameValuePair d : data) {
        if (d != null) {
          dataList.add(d);
        }
      }
    }
    return this;
  }

  @Override public BetterHttpRequest data(final Map<?, ?> data) {
    ensureNoEntity();

    final List<NameValuePair> dataList = getData();
    for (Entry<?, ?> entry : data.entrySet()) {
      final String name = entry.getKey().toString();
      final String value = entry.getValue().toString();
      dataList.add(new BasicNameValuePair(name, value));
    }
    return this;
  }

  protected List<NameValuePair> getData() {
    if (data == null) {
      data = new ArrayList<NameValuePair>();
    }
    return data;
  }

  private void ensureNoEntity() {
    if (entity != null) {
      throw new IllegalStateException(
          "You cannot set the data after specifying a custom entity.");
    }
  }

  private void ensureNoData() {
    if (data != null) {
      throw new IllegalStateException(
          "You cannot specify the entity after setting POST data.");
    }
  }
  
  protected void prepareRequest() throws IOException {
    super.prepareRequest();
    if (data != null) {
      entity = new UrlEncodedFormEntity(data, charset);
    }
    
    ((HttpPost)request).setEntity(entity);
  }
  
}
