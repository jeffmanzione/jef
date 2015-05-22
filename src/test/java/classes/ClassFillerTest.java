package classes;

import java.io.IOException;
import java.util.Queue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jeffreymanzione.jef.classes.ClassFiller;
import com.jeffreymanzione.jef.parsing.DoesNotConformToDefintionException;
import com.jeffreymanzione.jef.parsing.Parser;
import com.jeffreymanzione.jef.parsing.ParsingException;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.test.entities.Test1;
import com.jeffreymanzione.jef.test.entities.Test2;
import com.jeffreymanzione.jef.tokenizing.Token;
import com.jeffreymanzione.jef.tokenizing.TokenizeException;
import com.jeffreymanzione.jef.tokenizing.Tokenizer;
import com.jeffreymanzione.jef.tokenizing.TokenizerTest;

public class ClassFillerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws IOException, TokenizeException, ParsingException, DoesNotConformToDefintionException, InstantiationException, IllegalAccessException {
		ClassFiller cf = new ClassFiller();
		cf.addEntityClass(Test1.class);
		cf.addEntityClass(Test2.class);
		
		Parser parser = new Parser();
		
		Queue<Token> tokens = new Tokenizer().tokenize(TokenizerTest.class.getResourceAsStream("/test2.in.jef"));

		MapValue mappings = parser.parse(tokens, false);
		
		//System.out.println(mappings.get("a1"));
		
		Test1 a1 = cf.create((MapValue) mappings.get("a1"), Test1.class);
		Test2 a2 = cf.create((MapValue) mappings.get("a2"), Test2.class);

		System.out.println(a1);

		System.out.println(a2);
	}

}
