package com.jeffreymanzione.jef.parsing.value;

import java.util.ArrayList;
import java.util.List;

public class ListValue extends Value<List<Value<?>>> {

	List<Value<?>> values = new ArrayList<>();
	
	public ListValue() {
		super(ValueType.LIST);
		super.set(values);
	}
	
	public void add(Value<?> value) {
		values.add(value);
	}
	
}
