package com.jeffreymanzione.jef.parsing.value;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class MapValue extends Value<Map<String, Value<?>>> implements Iterable<Pair<?>> {
	public MapValue() {
		super(ValueType.MAP);
	}

	private Map<String, Value<?>> map = new HashMap<>();

	public void add(Pair<?> pair) {
		map.put(pair.getKey(), pair.getValue());
		super.set(map);
	}

	public Value<?> get(String key) {
		return map.get(key);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Iterator<Pair<?>> iterator() {
		return new Iterator<Pair<?>>() {

			Queue<Pair<?>> queue;
			{
				queue = new LinkedList<Pair<?>>();
				for (String key : map.keySet()) {
					queue.add(new Pair(key, map.get(key)));
				}
			}

			@Override
			public boolean hasNext() {
				return !queue.isEmpty();
			}

			@Override
			public Pair<?> next() {
				return queue.remove();
			}
		};

	}

}
