package com.jeffreymanzione.jef.parsing.value;

public class Pair<T> {
	private final String key;
	private final Value<T> value;

	public Pair(String key, Value<T> value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public Value<T> getValue() {
		return value;
	}

	public String toString() {
		return "Key=" + key + ", Value=" + value;
	}
}
