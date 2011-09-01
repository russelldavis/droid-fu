package com.github.droidfu.util;

import java.util.LinkedHashMap;
import org.json.JSONObject;

public class FuLinkedMap<K, V> extends LinkedHashMap<K, V> {
  private static final long serialVersionUID = 1L;

  public FuLinkedMap<K, V> set(K key, V value) {
    put(key, value);
    return this;
  }

  public V opt(K key, V defaultValue) {
    return FuMap.opt(this, key, defaultValue);
  }

  public JSONObject toJson() {
    return FuMap.toJson(this);
  }
}
