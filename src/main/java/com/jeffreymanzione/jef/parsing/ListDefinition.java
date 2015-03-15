package com.jeffreymanzione.jef.parsing;


public class ListDefinition extends Definition {
	private Definition type;

	public ListDefinition(Definition type) {
		this.type = type;
	}

	public Definition getType() {
		return type;
	}
}
