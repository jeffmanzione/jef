package com.jeffreymanzione.jef.assembly;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jeffreymanzione.jef.classes.ClassFiller;
import com.jeffreymanzione.jef.parsing.Parser;
import com.jeffreymanzione.jef.test.entities.Test1;
import com.jeffreymanzione.jef.test.entities.Test2;
import com.jeffreymanzione.jef.tokenizing.Tokenizer;

public class AssemblerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		Assembler assembler = new Assembler();
		assembler.setTokenizer(new Tokenizer());
		assembler.setParser(new Parser());
		ClassFiller cf = new ClassFiller();
		cf.addEntityClass(Test1.class, Test2.class);
		assembler.setFiller(cf);
		assembler.setSource(AssemblerTest.class.getResourceAsStream("/test2.in.jef"));
		Map<String, Object> map = assembler.assemble();
		
		for (Entry<String, Object> entry : map.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
	}

}
