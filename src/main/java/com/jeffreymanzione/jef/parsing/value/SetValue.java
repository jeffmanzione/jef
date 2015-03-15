package com.jeffreymanzione.jef.parsing.value;

import java.util.HashSet;
import java.util.Set;

public class SetValue extends Value<Set<Value<?>>> {
	private Set<Value<?>> values = new HashSet<>();
	
	public SetValue() {
		super(ValueType.SET);
		super.set(values);
	}
	
	public void add(Value<?> value) {
		values.add(value);
	}
}
