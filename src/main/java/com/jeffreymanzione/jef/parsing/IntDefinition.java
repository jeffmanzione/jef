package com.jeffreymanzione.jef.parsing;

public class IntDefinition extends SingletonDefintion {

	public static volatile IntDefinition instance;

	private IntDefinition() {
		super("LONG");
	}

	public static Definition instance() {
		synchronized (IntDefinition.class) {
			if (instance == null) {
				instance = new IntDefinition();
			}
		}

		return instance;
	}

}
