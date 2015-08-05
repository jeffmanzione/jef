package com.jeffreymanzione.jef.parsing;

public class BooleanDefinition extends EnumDefinition {
  private static BooleanDefinition instance;

  private BooleanDefinition() {
    this.add("true");
    this.add("false");
    this.setName("Bool");
  }

  public static Definition instance() {
    synchronized (BooleanDefinition.class) {
      if (instance == null) {
        instance = new BooleanDefinition();
      }
    }

    return instance;
  }
}
