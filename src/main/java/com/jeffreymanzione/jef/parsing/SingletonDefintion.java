package com.jeffreymanzione.jef.parsing;

import java.util.Map;

public abstract class SingletonDefintion extends Definition {
  private Class<?> cls;

  public SingletonDefintion (String name, Class<?> cls) {
    this.cls = cls;
    this.setName(name);
  }

  public void validateInnerTypes (Map<String, Definition> definitions) {
  }

  public Class<?> getRepresentedClass () {
    return cls;
  }

}
