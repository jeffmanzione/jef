package com.jeffreymanzione.jef.parsing;

import java.util.Map;

import com.jeffreymanzione.jef.resurrection.BuiltInResurrector.Transformer;

public class BuiltInDefinition<T> extends Definition {

  private Transformer<T> trans;

  public BuiltInDefinition(Transformer<T> trans) {
    this.setName(trans.id);
    this.trans = trans;
  }

  public Definition getInnerDefintion() {
    return trans.internalDef;
  }

  @Override
  protected void validateInnerTypes(Map<String, Definition> definitions) {

  }

}
