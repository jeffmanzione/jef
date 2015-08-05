package com.jeffreymanzione.jef.parsing.exceptions;

import com.jeffreymanzione.jef.parsing.value.Value;
import com.jeffreymanzione.jef.tokenizing.Token;

public class DoesNotConformToDefintionException extends IndexableException {
  /**
	 * 
	 */
  private static final long serialVersionUID = -1895815961800477678L;

  private final Value<?>    value;
  private final Token       token;

  public DoesNotConformToDefintionException(Value<?> value, String message) {
    super(value, message);
    this.value = value;
    this.token = value.getToken();
  }

  public Value<?> getValue() {
    return value;
  }

  public Token getToken() {
    return token;
  }

}
