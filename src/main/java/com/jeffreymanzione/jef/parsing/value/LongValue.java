package com.jeffreymanzione.jef.parsing.value;

public class LongValue extends PrimitiveValue<Long> {

	public LongValue(long val) {
		super(ValueType.LONG);
		super.set(val);
	}

}
