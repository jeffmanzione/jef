package com.jeffreymanzione.jef.parsing.value.primitive;

import com.jeffreymanzione.jef.parsing.value.ValueType;
import com.jeffreymanzione.jef.tokenizing.Token;

public class BooleanValue extends PrimitiveValue<Boolean> {
  public BooleanValue (boolean val, Token token) {
    super(ValueType.BOOL, token);
    super.set(val);
  }
}
