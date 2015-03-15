package com.jeffreymanzione.jef.parsing.value;

public class LongValue extends Value<Long> {

	public LongValue(long val) {
		super(ValueType.LONG);
		super.set(val);
	}

}
