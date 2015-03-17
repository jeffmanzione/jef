package com.jeffreymanzione.jef.parsing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


//import parsing.value.ValueType;

public class MapDefinition extends Definition implements Iterable<String> {
	// private Map<String, ValueType> referenced = new HashMap<>();
	private Map<String /* Integer */, Definition> defs = new HashMap<>();

	private Definition restriction;

	public boolean isRestricted() {
		return restriction != null;
	}

	public Definition getRestriction() {
		return restriction;
	}

	// public void add(String name, ValueType type) {
	// referenced.put(name, type);
	// }

	public void add(String name, Definition def) {
		defs.put(/* referenced.size() */name, def);
		// referenced.put(name, ValueType.DEFINED);
	}

	public Definition get(String key) {
		return defs.get(key);
	}

	@Override
	public Iterator<String> iterator() {
		return defs.keySet().iterator();
	}

	public void setRestricted(Definition restriction) {
		this.restriction = restriction;

	}

}
