package com.jeffreymanzione.jef.parsing.value;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.jeffreymanzione.jef.tokenizing.Token;

public class SetValue extends Value<Set<Value<?>>> implements Iterable<Value<?>> {
	private Set<Value<?>> values = new HashSet<>();
	
	public SetValue(Token token) {
		super(ValueType.SET, token);
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
