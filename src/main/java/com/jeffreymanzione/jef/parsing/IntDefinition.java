package com.jeffreymanzione.jef.parsing;

public class IntDefinition extends Definition {
	
	public static final IntDefinition singleton = new IntDefinition();
	
	private IntDefinition() {
		
	}
	
	public static IntDefinition instance() {
		return singleton;
	}
}
