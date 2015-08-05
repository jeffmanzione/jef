package com.jeffreymanzione.jef.parsing.value;

import com.jeffreymanzione.jef.tokenizing.IndexedObject;
import com.jeffreymanzione.jef.tokenizing.Token;

public abstract class Value<T> extends IndexedObject {

  private final ValueType type;
  private final Token     token;
  private T               value;

  protected Value(ValueType type, Token token) {
    super(token);
    this.type = type;
    this.token = token;
  }

  public Token getToken() {
    return token;
  }

  protected void set(T value) {
    this.value = value;
  }

  public ValueType getType() {
    return type;
  }

  public T getValue() {
    return value;
  }

  private String entityID;

  public String getEntityID() {
    return entityID;
  }

  public void setEntityID(String entityID) {
    this.entityID = entityID;
  }

  public boolean hasEntityID() {
    return entityID != null;
  }

  public String toStringType() {
    return type.toString();
  }

  public String toString() {
    return value.toString();
  }
}
