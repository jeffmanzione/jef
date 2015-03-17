package com.jeffreymanzione.jef.tokenizing;

public final class Token {

	private final Word word;
	private final TokenType type;

	public Token(Word text, TokenType type) {
		super();
		this.word = text;
		this.type = type;
	}

	public String getText() {
		return word.getText();
	}
	
	public int getLine() {
		return word.getLine();
	}
	
	public int getColumn() {
		return word.getColumn();
	}

	public TokenType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Token [text='" + word.getText() + "', type=" + type + "]";
	}
}
