package com.jeffreymanzione.jef.parsing.value;

import com.jeffreymanzione.jef.parsing.Definition;

@Deprecated
public class TypeValue {
	private final Definition def;
	
	public TypeValue(Definition def) {
		this.def = def;
	}

	public Definition getDef() {
		return def;
	}
}
