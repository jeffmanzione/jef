package com.jeffreymanzione.jef.test.entities;

import java.util.Arrays;

import com.jeffreymanzione.jef.resurrection.JEFEntityMap;

public class ArrayTest2 extends JEFEntityMap {
  private String[][] stringArray;
  private int[]      intArray;

  @Override
  public String toString () {
    return "ArrayTest [stringArray=" + Arrays.toString(stringArray)
        + ", intArray=" + Arrays.toString(intArray) + "]";
  }
}
