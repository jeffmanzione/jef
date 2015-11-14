package com.jeffreymanzione.jef.parsing;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Queue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jeffreymanzione.jef.parsing.exceptions.IndexableException;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.parsing.value.Pair;
import com.jeffreymanzione.jef.tokenizing.Token;
import com.jeffreymanzione.jef.tokenizing.TokenizeException;
import com.jeffreymanzione.jef.tokenizing.JEFTokenizer;
import com.jeffreymanzione.jef.tokenizing.Tokenizer;
import com.jeffreymanzione.jef.tokenizing.TokenizerTest;

public class ParsingTest {

  @Before
  public void setUp () throws Exception {
  }

  @After
  public void tearDown () throws Exception {
  }

  @Test
  public void test ()
      throws IndexableException, IOException, TokenizeException {
    Tokenizer tokenizer = new JEFTokenizer();
    // tokenizer.setVerbose(true);
    Queue<Token> tokens = tokenizer
        .tokenize(TokenizerTest.class.getResourceAsStream("/test2.in.jef"));

    Parser parser = new JEFParser();
    MapValue mappings = parser.parse(tokens);
    for (Pair<String, ?> p : mappings) {
      System.out.println(p.getValue());
    }
    if (mappings.get("a1") == null || mappings.get("a2") == null) {
      fail();
    }

  }

}
