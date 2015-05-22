package com.jeffreymanzione.jef.parsing.value;

public class FloatValue extends PrimitiveValue<Double> {
	
	public FloatValue(double val) {
		super(ValueType.FLOAT);
		super.set(val);
	}
	
}
