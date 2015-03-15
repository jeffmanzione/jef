package com.jeffreymanzione.jef.parsing.value;

public class FloatValue extends Value<Double> {
	
	public FloatValue(double val) {
		super(ValueType.FLOAT);
		super.set(val);
	}
	
}
