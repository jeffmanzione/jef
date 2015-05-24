package com.jeffreymanzione.jef.parsing.value;

public class EnumValue extends Value<String> {
	
	public EnumValue(String val) {
		super(ValueType.ENUM);
		super.set(val);
	}

	public String toString() {
		return super.toString();
	}
}
