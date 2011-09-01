package com.github.droidfu.support;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonSupport {
  
  /**
   * Performs a shallow copy of a JSONObject
   * @param json object to copy
   * @return JSONObject
   */
  @SuppressWarnings("unchecked")
  public static JSONObject clone(JSONObject json) {
    JSONObject clone = new JSONObject();
    Iterator<String> it = json.keys();
    try {
      while (it.hasNext()) {
        String key = it.next();
        clone.put(key, json.get(key));
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return clone;
  }
}
