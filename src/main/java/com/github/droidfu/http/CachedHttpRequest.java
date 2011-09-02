package com.github.droidfu.http;

import java.io.IOException;	
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONObject;

public class CachedHttpRequest implements BetterHttpRequest {

    private String url;

    public CachedHttpRequest(String url) {
        this.url = url;
    }

    public String getRequestUrl() {
        return url;
    }

    public BetterHttpRequest expecting(Integer... statusCodes) {
        return this;
    }

    public BetterHttpRequest retries(int retries) {
        return this;
    }

    public BetterHttpResponse send() throws IOException {
        return new CachedHttpResponse(url);
    }

    public HttpUriRequest unwrap() {
        return null;
    }

    public BetterHttpRequest withTimeout(int timeout) {
        return this;
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

    @Override public BetterHttpRequest customize(RequestCustomizer customizer) {
      return this;
    }

    @Override public BetterHttpRequest header(String name, String value) {
      return this;
    }

    @Override public BetterHttpRequest header(Header header) {
      return this;
    }

    @Override public BetterHttpRequest charset(String charset) {
      return this;
    }

    @Override public BetterHttpRequest followRedirects(boolean follow) {
      return this;
    }
}
