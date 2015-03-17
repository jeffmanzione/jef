package com.jeffreymanzione.jef.parsing.value;

public class StringValue extends Value<String> {
	public StringValue(String val) {
		super(ValueType.STRING);
		super.set(val);
	}

	public String toString() {
		return "'" + super.toString() + "'";
	}

}
