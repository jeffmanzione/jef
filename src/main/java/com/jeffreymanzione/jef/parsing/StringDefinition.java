package com.jeffreymanzione.jef.parsing;

public class StringDefinition extends SingletonDefintion {

  public static volatile StringDefinition instance;

  private StringDefinition() {
    super("String", String.class);
  }

  public static Definition instance() {
    synchronized (StringDefinition.class) {
      if (instance == null) {
        instance = new StringDefinition();
      }
    }

    return instance;
  }
}
