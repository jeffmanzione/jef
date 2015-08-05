package com.jeffreymanzione.jef.parsing.value.primitive;

import com.jeffreymanzione.jef.parsing.value.ValueType;
import com.jeffreymanzione.jef.tokenizing.Token;

public class StringValue extends PrimitiveValue<String> {
  public StringValue(String val, Token token) {
    super(ValueType.STRING, token);
    super.set(val);
  }

  public String toString() {
    return "'" + super.toString() + "'";
  }

}
