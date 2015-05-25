package com.jeffreymanzione.jef.parsing.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.jeffreymanzione.jef.parsing.ParsingException;
import com.jeffreymanzione.jef.parsing.TupleDefinition;
import com.jeffreymanzione.jef.parsing.TupleException;

public class TupleValue extends Value<List<Value<?>>> implements Iterable<Pair<Integer,?>> {

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
	
	public Value<?> get(int index) {
		return values.get(index);
	}
	
	public int size() {
		return values.size();
	}
	
	@Override
	public String toString() {
		String result = "(" + values.get(0).toString();

		for (int index = 1; index < values.size(); index++) {
			result += ", " + values.get(index).toString();
		}
		return result + ")";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<Pair<Integer, ?>> iterator() {
		return new Iterator<Pair<Integer,?>>() {

			Queue<Pair<Integer,?>> queue;
			{
				queue = new LinkedList<Pair<Integer,?>>();
				for (int i = 0; i < values.size(); i++) {
					queue.add(new Pair(i,values.get(i)));
				}
			}

			@Override
			public boolean hasNext() {
				return !queue.isEmpty();
			}

			@Override
			public Pair<Integer,?> next() {
				return queue.remove();
			}
		};

	}
}
