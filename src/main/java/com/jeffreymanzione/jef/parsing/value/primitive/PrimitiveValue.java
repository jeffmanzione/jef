package com.jeffreymanzione.jef.parsing.value.primitive;

import com.jeffreymanzione.jef.parsing.value.Value;
import com.jeffreymanzione.jef.parsing.value.ValueType;
import com.jeffreymanzione.jef.tokenizing.Token;

public abstract class PrimitiveValue<T> extends Value<T> {

	protected PrimitiveValue(ValueType type, Token token) {
		super(type, token);
	}

}
