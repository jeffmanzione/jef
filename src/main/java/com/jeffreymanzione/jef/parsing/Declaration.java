package com.jeffreymanzione.jef.parsing;

public class Declaration {

  private final Definition type;
  private final String     name;

  public Declaration (Definition definition, String name) {
    super();
    this.type = definition;
    this.name = name;
  }

  public Definition getDefinition () {
    return type;
  }

  public String getName () {
    return name;
  }

  public String toString () {
    return "Declaration(name=" + name + ",def=" + type + ")";
  }
}
