package com.jeffreymanzione.jef.test.entities;

import java.util.Arrays;

import com.jeffreymanzione.jef.resurrection.JEFEntityMap;

public class ArrayTest extends JEFEntityMap {
  private String[] stringArray;
  private Integer[] intArray;
  
  @Override
  public String toString() {
    return "ArrayTest [stringArray=" + Arrays.toString(stringArray) + ", intArray="
        + Arrays.toString(intArray) + "]";
  }
}
