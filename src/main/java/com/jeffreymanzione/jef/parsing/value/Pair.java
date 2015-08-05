package com.jeffreymanzione.jef.parsing.value;

public class Pair<K, T> {
  private final K        key;
  private final Value<T> value;

  public Pair(K key, Value<T> value) {
    super();
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public Value<T> getValue() {
    return value;
  }

  public String toString() {
    return "Key=" + key + ", Value=" + value;
  }
}
