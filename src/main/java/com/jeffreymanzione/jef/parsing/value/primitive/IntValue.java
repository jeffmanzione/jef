package com.jeffreymanzione.jef.parsing.value.primitive;

import com.jeffreymanzione.jef.parsing.value.ValueType;
import com.jeffreymanzione.jef.tokenizing.Token;

public class IntValue extends PrimitiveValue<Integer> {
  public IntValue(int val, Token token) {
    super(ValueType.INT, token);
    super.set(val);
  }
}
