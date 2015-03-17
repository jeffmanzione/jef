package com.jeffreymanzione.jef.parsing;

public class DoesNotConformToDefintionException extends Exception {

	public DoesNotConformToDefintionException(int line, int column, String string) {
		super("On line " + line + " column " + column + ": " + string);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1895815961800477678L;

}
