package com.jeffreymanzione.jef.resurrection;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jeffreymanzione.jef.parsing.JEFParser;
import com.jeffreymanzione.jef.parsing.Parser;
import com.jeffreymanzione.jef.parsing.value.ArrayValue;
import com.jeffreymanzione.jef.parsing.value.MapValue;
import com.jeffreymanzione.jef.parsing.value.primitive.IntValue;
import com.jeffreymanzione.jef.resurrection.Resurrector;
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

    Queue<Token> tokens = new JEFTokenizer().tokenize(ResurrectorTest.class
        .getResourceAsStream("/test2.in.jef"));

    MapValue mappings = parser.parse(tokens);

    // System.out.println(mappings.get("a1"));

    Test1 a1 = cf.parseToObject((MapValue) mappings.get("a1"));
    Test2 a2 = cf.parseToObject((MapValue) mappings.get("a2"));

    System.out.println(a1);

    System.out.println(a2);
  }

  @Test
  public void testArray() throws Exception {
    Resurrector cf = new Resurrector();
    
    Token token = new Token(new Word("nop", new StringBuilder(), 0, 0), TokenType.NOP);
    
    ArrayValue<Integer> val = new ArrayValue<Integer>(token);
    val.add(new IntValue(0, token));
    val.add(new IntValue(1, token));
    val.add(new IntValue(2, token));
    val.add(new IntValue(3, token));
    val.add(new IntValue(4, token));
    val.add(new IntValue(5, token));
    Integer[] arr = (Integer[]) cf.parseToObject(val);
    for (Integer integer : arr) {
      System.out.println(integer);
    }
  }
  
  @Test
  public void testReflect() throws Exception {
    List<List<Integer>> llint = new ArrayList<>();
    
    Class<?> intCls = ((Class<?>) ((ParameterizedType) llint.getClass()
        .getGenericSuperclass()).getActualTypeArguments()[0]);
    System.out.println(intCls.toString());
  }
  
}
