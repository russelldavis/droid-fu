package com.github.droidfu.util;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class FuMap<K, V> extends HashMap<K, V> {
  private static final long serialVersionUID = 1L;

  public FuMap<K, V> set(K key, V value) {
    put(key, value);
    return this;
  }

  public V opt(K key, V defaultValue) {
    return opt(this, key, defaultValue);
  }

  public JSONObject toJson() {
    return toJson(this);
  }

  public static <K, V> V opt(Map<K, V> map, K key, V defaultValue) {
    V val = map.get(key);
    return val == null ? defaultValue : val;
  }
  
  public static <K, V> JSONObject toJson(Map<K, V> map) {
    JSONObject json = new JSONObject();
    for (Entry<K, V> entry : map.entrySet()) {
      V value = entry.getValue();
      try {
        if (value instanceof Map) {
          json.put((String) entry.getKey(), toJson((Map<?, ?>) value));
        } else {
          json.put((String) entry.getKey(), value);
        }
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }
    return json;
  }
   
}
