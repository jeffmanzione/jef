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

	public boolean hasKey(String name) {
		return defs.containsKey(name);
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

	
	public void validateInnerTypes(Map<String, Definition> definitions) {
		for (String varName : this) {
			Definition def = this.get(varName);
			//System.out.println(def.getClass().getSimpleName() + " " + (def instanceof TempDefinition));
			if (def instanceof TempDefinition) {
				this.add(varName, definitions.get(((TempDefinition) def).getName()));
			} else if (def instanceof MapDefinition || def instanceof ListDefinition || def instanceof TupleDefinition) {
				def.validateInnerTypes(definitions);
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			MapDefinition other;
			if (obj instanceof MapDefinition) {
				other = (MapDefinition) obj;
				return this.defs.equals(other.defs) && this.restriction.equals(other.restriction);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
