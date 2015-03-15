package com.jeffreymanzione.jef.parsing;

public class StringDefinition extends Definition {
	
	public static final StringDefinition singleton = new StringDefinition();
	
	private StringDefinition() {
		
	}
	
	public static StringDefinition instance() {
		return singleton;
	}
}
