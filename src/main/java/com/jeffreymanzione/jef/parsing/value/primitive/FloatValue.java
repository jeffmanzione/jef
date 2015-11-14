package com.jeffreymanzione.jef.parsing.value.primitive;

import com.jeffreymanzione.jef.parsing.value.ValueType;
import com.jeffreymanzione.jef.tokenizing.Token;

public class FloatValue extends PrimitiveValue<Double> {

  public FloatValue (double val, Token token) {
    super(ValueType.FLOAT, token);
    super.set(val);
  }

}
