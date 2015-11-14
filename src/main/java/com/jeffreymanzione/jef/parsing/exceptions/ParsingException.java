package com.jeffreymanzione.jef.parsing.exceptions;

import com.jeffreymanzione.jef.tokenizing.Indexable;
import com.jeffreymanzione.jef.tokenizing.Token;
import com.jeffreymanzione.jef.tokenizing.TokenType;

public class ParsingException extends IndexableException {

  private final Token       token;
  private final TokenType   expected;

  /**
   * 
   */
  private static final long serialVersionUID = -8845784388792588721L;

  public ParsingException (Token token, String message, TokenType expected) {
    super(token,
        "Expected token type " + expected + " but was " + token.getType()
            + ". Token contents=" + token + ". Message: " + message);
    this.token = token;
    this.expected = expected;
  }

  public ParsingException (Token token, String message) {
    super(token, "Token was " + token.getType() + "Token contents=" + token
        + ". Message: " + message);
    this.token = token;
    this.expected = null;
  }

  public ParsingException (String message, TokenType closeToken) {
    super(Indexable.EOF, message + " Expected token " + closeToken);
    this.token = null;
    this.expected = closeToken;
  }

  public Token getToken () {
    return token;
  }

  public TokenType getExpectedTokenType () {
    return expected;
  }

}
