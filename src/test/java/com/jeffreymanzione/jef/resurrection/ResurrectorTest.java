package com.jeffreymanzione.jef.resurrection;

import java.lang.reflect.Array;
import java.util.Queue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jeffreymanzione.jef.parsing.IntDefinition;
import com.jeffreymanzione.jef.parsing.JEFParser;
import com.jeffreymanzione.jef.parsing.Parser;
import com.jeffreymanzione.jef.parsing.value.ArrayValue;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.parsing.value.primitive.IntValue;
import com.jeffreymanzione.jef.resurrection.Resurrector;
import com.jeffreymanzione.jef.test.entities.ArrayTest1;
import com.jeffreymanzione.jef.test.entities.ArrayTest2;
import com.jeffreymanzione.jef.test.entities.Doge;
import com.jeffreymanzione.jef.test.entities.Test1;
import com.jeffreymanzione.jef.test.entities.Test2;
import com.jeffreymanzione.jef.test.entities.Tuple1;
import com.jeffreymanzione.jef.tokenizing.Token;
import com.jeffreymanzione.jef.tokenizing.JEFTokenizer;
import com.jeffreymanzione.jef.tokenizing.TokenType;
import com.jeffreymanzione.jef.tokenizing.Word;

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

    Parser parser = new JEFParser();

    Queue<Token> tokens = new JEFTokenizer()
        .tokenize(ResurrectorTest.class.getResourceAsStream("/test2.in.jef"));

    MapValue mappings = parser.parse(tokens);

    // System.out.println(mappings.get("a1"));

    Test1 a1 = cf.parseToObject((MapValue) mappings.get("a1"));
    Test2 a2 = cf.parseToObject((MapValue) mappings.get("a2"));

    System.out.println(a1);

    System.out.println(a2);
  }

  @Test
  public void testArray() throws Exception {

    // String[] strs = (String[]) new Object[] {"One", "Two", "Three"};

    Object strs = new String[] { "One", "Two", "Three" };

    Resurrector cf = new Resurrector();

    Token token = new Token(new Word("nop", new StringBuilder(), 0, 0), TokenType.NOP);

    ArrayValue val = new ArrayValue(token);
    val.add(new IntValue(0, token));
    val.add(new IntValue(1, token));
    val.add(new IntValue(2, token));
    val.add(new IntValue(3, token));
    val.add(new IntValue(4, token));
    val.add(new IntValue(5, token));
    val.setDefinedType(IntDefinition.instance());
    Integer[] arr = (Integer[]) cf.parseToObject(val);
    for (Integer integer : arr) {
      System.out.println(integer);
    }
  }

  @Test
  public void testArray2() throws Exception {
    Resurrector cf = new Resurrector();
    cf.addEntityClass(ArrayTest1.class);

    Parser parser = new JEFParser();

    Queue<Token> tokens = new JEFTokenizer()
        .tokenize(ResurrectorTest.class.getResourceAsStream("/test5.in.jef"));

    MapValue mappings = parser.parse(tokens);

    ArrayTest1 a1 = cf.parseToObject((MapValue) mappings.get("arrTest1"));

    System.out.println(a1);
    System.out.println(JEFEntity.toJEFEntityHeader(a1.getClass()));

  }
  
  @Test
  public void testArray3() throws Exception {
    Resurrector cf = new Resurrector();
    cf.addEntityClass(ArrayTest2.class);

    Parser parser = new JEFParser();

    Queue<Token> tokens = new JEFTokenizer()
        .tokenize(ResurrectorTest.class.getResourceAsStream("/test6.in.jef"));

    MapValue mappings = parser.parse(tokens);

    ArrayTest2 a1 = cf.parseToObject((MapValue) mappings.get("arrTest2"));

    System.out.println(a1);
    System.out.println(JEFEntity.toJEFEntityHeader(a1.getClass()));

  }

  @Test
  public void testRefXlect() throws Exception {
    // List<List<Integer>> llint = new ArrayList<>();
    //
    // Class<?> intCls = ((Class<?>) ((ParameterizedType) llint.getClass().getGenericSuperclass())
    // .getActualTypeArguments()[0]);
    // System.out.println(intCls.toString());
    //
    // Integer[][] ints = new Integer[10][10];
    // System.out.println(ints.getClass().getComponentType());
    System.out.println(new String[5][5].getClass());
    System.out.println(Array.newInstance(Array.newInstance(String.class, 5).getClass(), 5).getClass());
  }

}
