package com.jeffreymanzione.jef.classes;

public class CouldNotUpdateEntityMapException extends ClassFillingException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -47297391878701983L;

	/**
	 * 
	 */
	
	public CouldNotUpdateEntityMapException(String string) {
		super("Error: could not fill class: " + string);
	}
}
