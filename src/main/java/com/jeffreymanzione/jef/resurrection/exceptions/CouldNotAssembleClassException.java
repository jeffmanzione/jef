package com.jeffreymanzione.jef.resurrection.exceptions;

public class CouldNotAssembleClassException extends ClassFillingException {

  /**
	 * 
	 */
  private static final long serialVersionUID = -2474807668683620116L;

  public CouldNotAssembleClassException(String string) {
    super("Error: could not assemble class: " + string);
  }
}
