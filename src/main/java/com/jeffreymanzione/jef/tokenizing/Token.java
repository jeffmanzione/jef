package com.jeffreymanzione.jef.tokenizing;

public class Token {

	final String text;
	final TokenType type;

	public Token(String text, TokenType type) {
		super();
		this.text = text;
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public TokenType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Token [text='" + text + "', type=" + type + "]";
	}
}
