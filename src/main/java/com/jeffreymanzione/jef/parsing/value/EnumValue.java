package com.jeffreymanzione.jef.parsing.value;

import com.jeffreymanzione.jef.tokenizing.Token;

public class EnumValue extends Value<String> {

  public EnumValue (String val, Token token) {
    super(ValueType.ENUM, token);
    super.set(val);
  }

  public String toString () {
    return super.toString();
  }
}
