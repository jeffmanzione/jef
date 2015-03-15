package com.jeffreymanzione.jef.parsing;

import com.jeffreymanzione.jef.tokenizing.Token;

public class ParsingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8845784388792588721L;
	
	public ParsingException(Token token, String message) {
		super(token + " " + message);
	}

}
