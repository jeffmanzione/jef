package com.jeffreymanzione.jef.parsing.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.jeffreymanzione.jef.tokenizing.Token;

public class ListValue extends Value<List<Value<?>>> implements Iterable<Value<?>> {

	List<Value<?>> values = new ArrayList<>();
	
	public ListValue(Token token) {
		super(ValueType.LIST, token);
		super.set(values);
	}
	
	public void add(Value<?> value) {
		values.add(value);
	}

	@Override
	public Iterator<Value<?>> iterator() {
		return new Iterator<Value<?>>() {

			Queue<Value<?>> queue;
			{
				queue = new LinkedList<Value<?>>();
				for (Value<?> val : values) {
					queue.add(val);
				}
			}

			@Override
			public boolean hasNext() {
				return !queue.isEmpty();
			}

			@Override
			public Value<?> next() {
				return queue.remove();
			}
		};
	}
	
}
