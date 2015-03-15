package com.jeffreymanzione.jef.parsing.value;

public abstract class Value<T> {

	private ValueType type;
	private T value;

	protected Value(ValueType type) {
		this.type = type;
	}

	protected void set(T value) {
		this.value = value;
	}

	public ValueType getType() {
		return type;
	}

	public T getValue() {
		return value;
	}

	public String toStringType() {
		return type.toString();
	}

	public String toString() {
		return value.toString();
	}
}
