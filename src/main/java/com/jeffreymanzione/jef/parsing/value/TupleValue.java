package com.jeffreymanzione.jef.parsing.value;

import java.util.ArrayList;
import java.util.List;

import com.jeffreymanzione.jef.parsing.ParsingException;
import com.jeffreymanzione.jef.parsing.TupleDefinition;
import com.jeffreymanzione.jef.parsing.TupleException;

public class TupleValue extends Value<List<Value<?>>> {

	private List<Value<?>> values = new ArrayList<>();

	public TupleValue() {
		super(ValueType.TUPLE);
		super.set(values);
	}

	public void add(Value<?> value) throws ParsingException {
		values.add(value);
	}

	public void validate(TupleDefinition format) throws TupleException {

		if (format.length() != values.size()) {
			throw new TupleException("Tuple does not fit size requirement of format.");
		}

		for (int i = 0; i < values.size(); i++) {
			if (format.getTypeAt(i) != values.get(i).getType()) {
				throw new TupleException("Tuple type does not match.");
			}
		}
	}

	@Override
	public String toStringType() {
		String result = "(" + values.get(0).toStringType();

		for (int index = 1; index < values.size(); index++) {
			result += ", " + values.get(index).toStringType();
		}
		return result + ")";
	}
	
	@Override
	public String toString() {
		String result = "(" + values.get(0).toString();

		for (int index = 1; index < values.size(); index++) {
			result += ", " + values.get(index).toString();
		}
		return result + ")";
	}
}
