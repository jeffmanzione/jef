package com.jeffreymanzione.jef.parsing;

import java.util.Map;

public class TempDefinition extends Definition {
  public TempDefinition (String name) {
    this.setName(name);
  }

  @Override
  public void validateInnerTypes (Map<String, Definition> definitions) {
    // TODO Auto-generated method stub

  }

  @Override
  public String toString () {
    return "TempDefinition(" + getName() + ")";
  }

}
