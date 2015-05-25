package com.jeffreymanzione.jef.assembly;

import java.io.File;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jeffreymanzione.jef.classes.ClassFiller;
import com.jeffreymanzione.jef.parsing.Parser;
import com.jeffreymanzione.jef.test.entities.Datapoint;
import com.jeffreymanzione.jef.test.entities.Doge;
import com.jeffreymanzione.jef.test.entities.Event;
import com.jeffreymanzione.jef.test.entities.Test1;
import com.jeffreymanzione.jef.test.entities.Test2;
import com.jeffreymanzione.jef.test.entities.Tuple1;
import com.jeffreymanzione.jef.tokenizing.Tokenizer;

public class AssemblerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		Assembler assembler = new Assembler();
		assembler.setTokenizer(new Tokenizer());
		assembler.setParser(new Parser());
		ClassFiller cf = new ClassFiller();
		cf.addEntityClass(Test1.class, Test2.class, Tuple1.class);
		cf.addEnumClass(Doge.class);
		assembler.setFiller(cf);
		assembler.setSource(AssemblerTest.class.getResourceAsStream("/test2.in.jef"));
		Map<String, Object> map = assembler.assemble();
		System.out.println(map.get("a1"));
		System.out.println(cf.convertToJEFEntityFormat(map, false, -1));
	}

	@Test
	public void test2() throws Exception {
		Assembler assembler = new Assembler();
		Tokenizer tokenizer = new Tokenizer();
		// tokenizer.setVerbose(true);
		assembler.setTokenizer(tokenizer);
		assembler.setParser(new Parser());
		ClassFiller cf = new ClassFiller();
		cf.addEntityClass(Event.class, Datapoint.class);
		assembler.setFiller(cf);
		assembler.setSource(AssemblerTest.class.getResourceAsStream("/test3.in.jef"));
		Map<String, Object> map = assembler.assemble();
		System.out.println(cf.convertToJEFEntityFormat(map));
		cf.writeToFile(map, new File("test.out.jef"));
	}

}
