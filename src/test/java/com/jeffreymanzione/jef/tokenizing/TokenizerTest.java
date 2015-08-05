package com.jeffreymanzione.jef.tokenizing;

// import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Queue;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jeffreymanzione.jef.tokenizing.Token;
import com.jeffreymanzione.jef.tokenizing.TokenizeException;
import com.jeffreymanzione.jef.tokenizing.JEFTokenizer;

public class TokenizerTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  // @Test
  // public void build() throws IOException, TokenizeException {
  // Queue<Token> tokens =
  // Tokenizer.tokenize(TokenizerTest.class.getResourceAsStream("/test1.in.jef"), false);
  // try (PrintWriter out = new PrintWriter("C://Users/Jeff/git/jef/src/main/resources/test1.out"))
  // {
  // for (Token token : tokens) {
  // out.println(token);
  // }
  // }
  // }

  @Test
  public void test1() throws IOException, TokenizeException {
    Queue<Token> tokens = new JEFTokenizer().tokenize(TokenizerTest.class
        .getResourceAsStream("/test1.in.jef"));

    try (Scanner scanner = new Scanner(TokenizerTest.class.getResourceAsStream("/test1.out"))) {

      for (Token token : tokens) {
        // if (scanner.hasNext()) {
        // String line = scanner.nextLine();
        // if (!line.equals(token.toString())) {
        // // System.out.println(line);
        // // System.out.println(token.toString());
        // // fail();
        // }
        // } else {
        // System.err.println("Expected another token " + token);
        // fail();
        // }
        System.out.println(token.toString());
      }

      // if (scanner.hasNext()) {
      // fail();
      // }
    }

  }

}
