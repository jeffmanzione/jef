package com.jeffreymanzione.jef.parsing;

public class Modification {
  private ModificationType type;
  private Modification     innerModification;

  public Modification(ModificationType type) {
    this.type = type;
    this.innerModification = null;
  }

  public ModificationType getType() {
    return type;
  }

  public Modification getInnerModification() {
    return innerModification;
  }

  public void setInnerModification(Modification innerModification) {
    this.innerModification = innerModification;
  }

  public boolean hasInnerModification() {
    return innerModification != null;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();

    result.append(type.toString());
    if (hasInnerModification()) {
      result.append(" of " + innerModification.toString());
    }
    return result.toString();
  }

}
