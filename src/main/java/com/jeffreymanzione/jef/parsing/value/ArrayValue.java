package com.jeffreymanzione.jef.parsing.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jeffreymanzione.jef.tokenizing.Token;

public class ArrayValue extends Value<List<Value<?>>> implements Iterable<Value<?>> {
  private List<Value<?>> values = new ArrayList<>();

  public ArrayValue(Token token) {
    super(ValueType.ARRAY, token);
    super.set(values);
  }

  public void add(Value<?> value) {
    values.add(value);
  }

  @Override
  public Iterator<Value<?>> iterator() {
    return values.iterator();
  }
  
  public int size() {
    return values.size();
  }
}
