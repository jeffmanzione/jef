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

public class TokenizerTests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test1() throws IOException, TokenizeException {
		Queue<Token> tokens = Tokenizer.tokenize(TokenizerTests.class.getResourceAsStream("test1.in.jef"), true);

		try (Scanner scanner = new Scanner(TokenizerTests.class.getResourceAsStream("test1.out"))) {

			for (Token token : tokens) {
				if (scanner.hasNext()) {
					//System.out.println(scanner.nextLine());
					if (!scanner.nextLine().equals(token.toString())) {
						fail();
					}
				} else {
					fail();
				}
			}
		}

	}

}
