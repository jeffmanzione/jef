package com.jeffreymanzione.jef.parsing;

import java.util.Queue;

import com.jeffreymanzione.jef.parsing.exceptions.IndexableException;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.tokenizing.Token;

/**
 * 
 * @author Jeffrey J. Manzione
 * 
 *         A Parser takes in {@link Queue}<{@link Token}> and outputs a {@link MapValue} which represents those tokens.
 *         The starting point for a Parser is {@link #parse(Queue)}.
 *
 */
public interface Parser {
	/**
	 * If parse has been called, hasErrors() indicates whether or not there were errors in parsing. If
	 * {@link #parse(Queue)} has not been run, it returns false.
	 * 
	 * @return true if there were errors during parsing, false otherwise.
	 */
	public boolean hasErrors ();

	/**
	 * 
	 * @return A {@link ValidateResponse} which includes all validation information after {@link #parse(Queue)} has been
	 *         run.
	 */
	public ValidationResponse getExceptions ();

	/**
	 * Sets the verbosity of the parser. The implementation determines exactly how verbose "verbose" is.
	 * @param isVerbose
	 */
	public void setVerbose ( boolean isVerbose );
	/**
	 * 
	 * @return	true if the parse has been set to be verbose, false otherwise.
	 */
	public boolean isVerbose ();

	/**
	 * Parses a {@link Queue}<{@link Token}> and outputs a {@link MapValue} which represents the parsed tokens.
	 * @param tokens
	 * @return
	 * @throws IndexableException
	 */
	public MapValue parse ( Queue<Token> tokens ) throws IndexableException;
}
