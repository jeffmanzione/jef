package com.jeffreymanzione.jef.tokenizing;

public final class Token extends IndexedObject {

	private final Word word;
	private final TokenType type;

	public Token(Word text, TokenType type) {
		super(text);
		this.word = text;
		this.type = type;
	}

	public TokenType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Token [text='" + word.getText() + "', type=" + type + "]";
	}
}
