package com.jeffreymanzione.jef.parsing;

public class FloatDefinition extends Definition {
	public static final FloatDefinition singleton = new FloatDefinition();

	private FloatDefinition() {

	}

	public static FloatDefinition instance() {
		return singleton;
	}
}
