package com.jeffreymanzione.jef.parsing;

import java.util.Map;

public class ListDefinition extends Definition {
  private Definition type;

  public ListDefinition (Definition type) {
    this.type = type;
  }

  public Definition getType () {
    return type;
  }

  public String toString () {
    return "<" + type.toString() + ", ...>";
  }

  public void validateInnerTypes (Map<String, Definition> definitions) {
    if (type instanceof TempDefinition) {
      type = definitions.get(((TempDefinition) type).getName());
    }
  }

}
