package com.jeffreymanzione.jef.resurrection;

import java.util.Queue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jeffreymanzione.jef.parsing.Parser;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.resurrection.Resurrector;
import com.jeffreymanzione.jef.test.entities.Doge;
import com.jeffreymanzione.jef.test.entities.Test1;
import com.jeffreymanzione.jef.test.entities.Test2;
import com.jeffreymanzione.jef.test.entities.Tuple1;
import com.jeffreymanzione.jef.tokenizing.Token;
import com.jeffreymanzione.jef.tokenizing.Tokenizer;

public class ResurrectorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		Resurrector cf = new Resurrector();
		cf.addEntityClass(Test1.class);
		cf.addEntityClass(Test2.class);
		cf.addEntityClass(Tuple1.class);
		cf.addEnumClass(Doge.class);

		Parser parser = new Parser();

		Queue<Token> tokens = new Tokenizer().tokenize(ResurrectorTest.class.getResourceAsStream("/test2.in.jef"));

		MapValue mappings = parser.parse(tokens);

		// System.out.println(mappings.get("a1"));

		Test1 a1 = cf.parseToObject((MapValue) mappings.get("a1"));
		Test2 a2 = cf.parseToObject((MapValue) mappings.get("a2"));

		System.out.println(a1);

		System.out.println(a2);
	}

}
