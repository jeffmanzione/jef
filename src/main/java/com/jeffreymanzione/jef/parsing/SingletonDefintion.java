package com.jeffreymanzione.jef.parsing;

import java.util.Map;

public abstract class SingletonDefintion extends Definition {

	public SingletonDefintion(String name) {
		this.setName(name);
	}

	public void validateInnerTypes(Map<String, Definition> definitions) {
	}

}
