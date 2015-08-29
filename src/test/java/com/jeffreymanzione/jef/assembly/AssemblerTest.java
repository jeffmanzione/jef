package com.jeffreymanzione.jef.assembly;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jeffreymanzione.jef.parsing.JEFParser;
import com.jeffreymanzione.jef.resurrection.Resurrector;
import com.jeffreymanzione.jef.resurrection.exceptions.ClassFillingException;
import com.jeffreymanzione.jef.test.entities.Datapoint;
import com.jeffreymanzione.jef.test.entities.Doge;
import com.jeffreymanzione.jef.test.entities.Event;
import com.jeffreymanzione.jef.test.entities.Test1;
import com.jeffreymanzione.jef.test.entities.Test2;
import com.jeffreymanzione.jef.test.entities.Tuple1;
import com.jeffreymanzione.jef.test.entities.ArrayTest;
import com.jeffreymanzione.jef.tokenizing.JEFTokenizer;
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
    assembler.setTokenizer(new JEFTokenizer());
    assembler.setParser(new JEFParser());
    Resurrector cf = new Resurrector();
    cf.addEntityClass(Test1.class, Test2.class, Tuple1.class);
    cf.addEnumClass(Doge.class);
    assembler.setFiller(cf);
    assembler.setSource(AssemblerTest.class.getResourceAsStream("/test2.in.jef"));
    Map<String, Object> map = assembler.assemble();
    System.out.println(((Test2) map.get("a2")));
    System.out.println(cf.convertToJEFEntityFormat(map, false, -1));
  }

  @Test
  public void test2() throws Exception {
    Assembler assembler = new Assembler();
    Tokenizer tokenizer = new JEFTokenizer();
    // tokenizer.setVerbose(true);
    assembler.setTokenizer(tokenizer);
    assembler.setParser(new JEFParser());
    Resurrector cf = new Resurrector();
    cf.addEntityClass(Event.class, Datapoint.class);
    assembler.setFiller(cf);
    assembler.setSource(AssemblerTest.class.getResourceAsStream("/test3.in.jef"));
    Map<String, Object> map = assembler.assemble();
    System.out.println(cf.convertToJEFEntityFormat(map));
    cf.writeToFile(map, new File("test.out.jef"));
  }

  @Test
  public void test3() throws Exception {
    Assembler assembler = new Assembler();
    assembler.setTokenizer(new JEFTokenizer());
    assembler.setParser(new JEFParser());
    Resurrector cf = new Resurrector();
    cf.addEntityClass(Test1.class, Test2.class, Tuple1.class);
    cf.addEnumClass(Doge.class);
    assembler.setFiller(cf);
    assembler.setSource(AssemblerTest.class.getResourceAsStream("/test4.in.jef"));
    try {
      assembler.assemble();
      fail();
    } catch (ClassFillingException e) {
    }
  }

  @Test
  public void testArray() throws Exception {
    Assembler assembler = new Assembler();
    assembler.setTokenizer(new JEFTokenizer());
    assembler.setParser(new JEFParser());
    Resurrector cf = new Resurrector();
    cf.addEntityClass(ArrayTest.class);
    assembler.setFiller(cf);
    assembler.setSource(AssemblerTest.class.getResourceAsStream("/test5.in.jef"));
    Map<String, Object> map = assembler.assemble();
    System.out.println(cf.convertToJEFEntityFormat(map));
  }
}
