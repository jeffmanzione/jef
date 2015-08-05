package com.jeffreymanzione.jef.parsing;

import com.jeffreymanzione.jef.parsing.value.ValueType;

public class ColorDefinition extends TupleDefinition {
  private static volatile ColorDefinition instance;

  private ColorDefinition() {
    this.add(ValueType.INT);
    this.add(ValueType.INT);
    this.add(ValueType.INT);
    this.add(ValueType.INT);
  }

  public static ColorDefinition instance() {
    synchronized (ColorDefinition.class) {
      if (instance == null) {
        instance = new ColorDefinition();
      }
    }
    return instance;
  }
}
