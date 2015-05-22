package com.jeffreymanzione.jef.parsing.value;

public abstract class PrimitiveValue<T> extends Value<T> {

	protected PrimitiveValue(ValueType type) {
		super(type);
	}

}
