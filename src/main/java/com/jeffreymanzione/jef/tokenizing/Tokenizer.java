package com.jeffreymanzione.jef.tokenizing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;

import com.jeffreymanzione.jef.parsing.Parser;

/**
 * Takes an input string or file in JEF and outputs annotated tokens which can be used by the {@link Parser} class.
 * 
 * @author Jeffrey J. Manzione
 * @date 2015/06/09
 * 
 */
public interface Tokenizer {

  /**
   * Sets whether to tokenizer should log details while tokenizing input.
   * 
   * @param isVerbose
   *          true if the tokenizer is to be 'verbose', false, otherwise.
   */
  public void setVerbose (boolean isVerbose);

  /**
   * Outputs whether the tokenizer logs details while tokenizing input.
   * 
   * @return true if the tokenizer is 'verbose', false, otherwise.
   */
  public boolean isVerbose ();

  /**
   * Tokenizes a string in JEF into tokens which can be used by {@link JEFParser#parse(Queue)}.
   * 
   * @param string
   *          String to be tokenized
   * @return A {@link Queue}<{@link Token}> created by tokenizing the input string.
   * @throws TokenizeException
   */
  public Queue<Token> tokenize (String string) throws TokenizeException;

  /**
   * Tokenizes a stream in JEF into tokens which can be used by {@link JEFParser#parse(Queue)}.
   * 
   * @param stream
   *          An {@link InputStream} to be tokenized
   * @return A {@link Queue}<{@link Token}> created by tokenizing the input string.
   * @throws TokenizeException
   */
  public Queue<Token> tokenize (InputStream stream)
      throws IOException, TokenizeException;

  /**
   * Tokenizes the contents of a file in JEF into tokens which can be used by {@link JEFParser#parse(Queue)}.
   * 
   * @param string
   *          A {@link File} with content to be tokenized
   * @return A {@link Queue}<{@link Token}> created by tokenizing the input string.
   * @throws TokenizeException
   */
  public Queue<Token> tokenize (File file)
      throws IOException, TokenizeException;
}
