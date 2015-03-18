package com.jeffreymanzione.jef.parsing;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Queue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.tokenizing.Token;
import com.jeffreymanzione.jef.tokenizing.TokenizeException;
import com.jeffreymanzione.jef.tokenizing.Tokenizer;
import com.jeffreymanzione.jef.tokenizing.TokenizerTest;

public class ParsingTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws ParsingException, DoesNotConformToDefintionException, IOException, TokenizeException {
		Queue<Token> tokens = Tokenizer.tokenize(TokenizerTest.class.getResourceAsStream("/test1.in.jef"), false);

		Parser parser = new Parser();

		MapValue mappings = parser.parseFile(tokens, false);
		if (mappings.get("entities") == null || mappings.get("properties") == null) {
			fail();
		}

	}

}
