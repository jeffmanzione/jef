package com.jeffreymanzione.jef.tokenizing;

// import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum TokenType {

  /* Keywords */
  TYPE("type"), ENUM("enum"), OF("of"), INCLUDE("include"),
  /* Operators */
  COLON(":"), COMMA(","), LPAREN("("), RPAREN(")"), LBRAC("["), RBRAC("]"), LTHAN("<"), 
  GTHAN(">"), EQUALS("="), LBRCE("{"), RBRCE("}"), DOLLAR("$"), WILDCARD("?"),
  /* Syntactical */
  QUOTE(null),
  /* Data types */
  STRING(null), INT(null), FLOAT(null),
  /* special words */
  DEF(null), VAR(null), ENUMVAL(null), NOP("nop");

  
  // TokenType utilities
  private static Map<String, TokenType> tokens;

  static {
    tokens = new HashMap<>();
    for (TokenType type : TokenType.values()) {
      tokens.put(type.seq, type);
    }
  }
  
  public static TokenType getToken(String seq) {
    return tokens.get(seq);
  }

  public static boolean isKeyword(String seq) {
    return tokens.containsKey(seq);
  }
  
  // TokenType class
  private String seq;

  TokenType(String seq) {
    this.seq = seq;
  }

  public String getString() {
    return seq;
  }

}
