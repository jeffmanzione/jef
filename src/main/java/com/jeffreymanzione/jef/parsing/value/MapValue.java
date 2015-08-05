package com.jeffreymanzione.jef.parsing.value;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.jeffreymanzione.jef.tokenizing.Token;

public class MapValue extends Value<Map<String, Value<?>>> implements Iterable<Pair<String, ?>> {
  public MapValue(Token token) {
    super(ValueType.MAP, token);
  }

  private Map<String, Value<?>> map = new HashMap<>();

  public void add(Pair<String, ?> pair) {
    map.put(pair.getKey(), pair.getValue());
    super.set(map);
  }

  public Value<?> get(String key) {
    return map.get(key);
  }

  public boolean hasKey(String key) {
    return map.containsKey(key);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Iterator<Pair<String, ?>> iterator() {
    return new Iterator<Pair<String, ?>>() {

      Queue<Pair<String, ?>> queue;
      {
        queue = new LinkedList<Pair<String, ?>>();
        for (String key : map.keySet()) {
          queue.add(new Pair(key, map.get(key)));
        }
      }

      @Override
      public boolean hasNext() {
        return !queue.isEmpty();
      }

      @Override
      public Pair<String, ?> next() {
        return queue.remove();
      }
    };

  }

}
