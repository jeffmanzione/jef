package com.jeffreymanzione.jef.parsing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jeffreymanzione.jef.parsing.value.ValueType;

public class TupleDefinition extends Definition {
	private List<ValueType> types = new ArrayList<>();

	public void add(ValueType type) {
		types.add(type);
	}

	public void add(Definition def) {
		if (def instanceof IntDefinition) {
			types.add(ValueType.LONG);
		} else if (def instanceof FloatDefinition) {
			types.add(ValueType.FLOAT);
		} else {
			types.add(ValueType.DEFINED);
		}
	}

	public int length() {
		return types.size();
	}

	public ValueType getTypeAt(int i) {
		return types.get(i);
	}

	public String toString() {
		Iterator<ValueType> tupleType = types.iterator();
		String result = tupleType.next().toString();
		while (tupleType.hasNext()) {
			result += ", " + tupleType.next();
		}

		return "(" + result + ")";
	}

}
