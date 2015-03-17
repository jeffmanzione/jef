package com.jeffreymanzione.jef.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.jeffreymanzione.jef.parsing.value.ValueType;

public class TupleDefinition extends Definition {
	private int size = 0;
	private List<ValueType> types = new ArrayList<>();
	private Map<Integer, Definition> toDefs = new HashMap<>();

	public void add(ValueType type) {
		types.add(type);
	}

	public void add(Definition def) {
		if (def instanceof IntDefinition) {
			types.add(ValueType.LONG);
		} else if (def instanceof FloatDefinition) {
			types.add(ValueType.FLOAT);
		} else if (def instanceof StringDefinition) {
			types.add(ValueType.STRING);
		} else {
			types.add(ValueType.DEFINED);
			toDefs.put(size, def);
		}

		size++;
	}

	public int length() {
		return types.size();
	}

	public ValueType getTypeAt(int i) {
		return types.get(i);
	}

	public Definition getDefinitionAt(int i) {
		// System.out.println("TO DEFS " + toDefs);
		return toDefs.get(i);
	}

	public String toString() {
		Iterator<ValueType> tupleType = types.iterator();

		String result;

		ValueType type = tupleType.next();

		if (type == ValueType.DEFINED) {
			result = toDefs.get(0).toString();
		} else {
			result = type.toString();
		}

		int index = 1;
		while (tupleType.hasNext()) {
			type = tupleType.next();
			if (type == ValueType.DEFINED) {
				//System.out.println("HMMM " + index + " " + toDefs);
				result += ", " + toDefs.get(index).toString();
			} else {
				result += ", " + type.toString();
			}
			index++;
		}

		//System.out.println("(" + result + ")");

		return "(" + result + ")";
	}

}
