package com.jeffreymanzione.jef.parsing.exceptions;

import com.jeffreymanzione.jef.tokenizing.Token;
import com.jeffreymanzione.jef.tokenizing.TokenType;

public class ParsingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8845784388792588721L;

	public ParsingException(Token token, String message, TokenType expected) {
		super("On line " + token.getLine() + " column " + token.getColumn() + ": Expected token type " + expected
				+ " but was " + token.getType() + ". Message: " + message);
	}

	public ParsingException(Token token, String message) {
		super("On line " + token.getLine() + " column " + token.getColumn() + ": Token was " + token.getType()
				+ ". Message: " + message);
	}

	public ParsingException(String message, TokenType closeToken) {
		super(message + " Expected token " + closeToken);
	}
}
