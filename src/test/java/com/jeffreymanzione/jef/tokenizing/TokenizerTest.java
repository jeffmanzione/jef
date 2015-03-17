package com.jeffreymanzione.jef.tokenizing;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Queue;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jeffreymanzione.jef.tokenizing.Token;
import com.jeffreymanzione.jef.tokenizing.TokenizeException;
import com.jeffreymanzione.jef.tokenizing.Tokenizer;

public class TokenizerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() throws IOException, TokenizeException {
		Queue<Token> tokens = Tokenizer.tokenize(TokenizerTest.class.getResourceAsStream("/test1.in.jef"), false);

		try (Scanner scanner = new Scanner(TokenizerTest.class.getResourceAsStream("/test1.out"))) {

			for (Token token : tokens) {
				if (scanner.hasNext()) {
					String line = scanner.nextLine();
					if (!line.equals(token.toString())) {
						fail();
					}
				} else {
					System.err.println("Expected another token " + token);
					fail();
				}
			}

			if (scanner.hasNext()) {
				fail();
			}
		}

	}

}
