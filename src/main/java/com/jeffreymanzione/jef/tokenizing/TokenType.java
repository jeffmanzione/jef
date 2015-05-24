package com.jeffreymanzione.jef.tokenizing;

//import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum TokenType {

	/* Keywords */
	TYPE("type"), ENUM("enum"), OF("of"),
	/* Operators */
	COLON(":"), COMMA(","), LPAREN("("), RPAREN(")"), LBRAC("["), RBRAC("]"), LTHAN(
			"<"), GTHAN(">"), EQUALS("="), LBRCE("{"), RBRCE("}"), DOLLAR("$"),
	/* Syntactical */
	STRING(null),

	LONG(null), FLOAT(null),

	QUOTE(null),
	
	DEF(null), VAR(null),
	ENUMVAL(null);
	


	private static Map<String, TokenType> tokens;

	static {
		tokens = new HashMap<>();

		//Arrays.asList(TokenType.values()).forEach(t -> tokens.put(t.seq, t));
		for (TokenType type : TokenType.values()) {
			tokens.put(type.seq,  type);
		}
		
	}
	
	private String seq;

	TokenType(String seq) {
		this.seq = seq;
	}
	
	public static TokenType getToken(String seq) {
		return tokens.get(seq);
	}

	public String getString() {
		return seq;
	}

	public static boolean isKeyword(String seq) {
		return tokens.containsKey(seq);
	}

}
