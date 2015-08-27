package com.jeffreymanzione.jef.parsing.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jeffreymanzione.jef.tokenizing.Token;

public class ArrayValue<T> extends Value<List<Value<T>>> implements Iterable<Value<T>> {
  private List<Value<T>> values = new ArrayList<>();

  public ArrayValue(Token token) {
    super(ValueType.ARRAY, token);
    super.set(values);
  }

  public void add(Value<T> value) {
    values.add(value);
  }

  @Override
  public Iterator<Value<T>> iterator() {
    return values.iterator();
  }
  
  public int size() {
    return values.size();
  }
}
