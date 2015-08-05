package com.jeffreymanzione.jef.parsing;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class EnumDefinition extends Definition {
  private Set<String> enumerables = new HashSet<>();

  public void add(String enumerable) {
    enumerables.add(enumerable);
  }

  public boolean contains(String enumerable) {
    return enumerables.contains(enumerable);
  }

  public String toString() {
    Iterator<String> enumVals = enumerables.iterator();
    String result = "";
    String prev = "'" + enumVals.next() + "'";
    int count = 1;
    while (enumVals.hasNext()) {
      count++;
      result += prev + ", ";
      prev = "'" + enumVals.next() + "'";
    }
    if (count == 2) {
      result = result.substring(0, result.length() - 2) + " ";
    }

    if (count > 1) {
      result += "or " + prev;
    }

    return result;
  }

  @Override
  public void validateInnerTypes(Map<String, Definition> definitions) {
    // TODO Auto-generated method stub

  }
}
