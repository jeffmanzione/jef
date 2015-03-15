package com.jeffreymanzione.jef.parsing;

public class ModDeclaration extends Declaration {

	private final Modification mod;
	
	public ModDeclaration(Definition definition, String name, Modification mod) {
		super(definition, name);
		this.mod = mod;
	}

	public Modification getMod() {
		return mod;
	}

}
