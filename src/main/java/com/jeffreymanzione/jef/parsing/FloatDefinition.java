package com.jeffreymanzione.jef.parsing;

public class FloatDefinition extends SingletonDefintion {

	public static volatile FloatDefinition instance;

	private FloatDefinition() {
		super("FLOAT");
	}

	public static Definition instance() {
		synchronized (FloatDefinition.class) {
			if (instance == null) {
				instance = new FloatDefinition();
			}
		}

		return instance;
	}
}
