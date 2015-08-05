package com.jeffreymanzione.jef.parsing;

import java.util.Queue;

import com.jeffreymanzione.jef.parsing.exceptions.IndexableException;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.tokenizing.Token;

public interface Parser {

  public boolean hasErrors();

  public ValidationResponse getExceptions();

  public void setVerbose(boolean isVerbose);

  public boolean isVerbose();

  public MapValue parse(Queue<Token> tokens) throws IndexableException;
}
