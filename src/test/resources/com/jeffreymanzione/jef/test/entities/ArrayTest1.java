package com.jeffreymanzione.jef.test.entities;

import java.util.Arrays;
import java.util.List;

import com.jeffreymanzione.jef.resurrection.JEFEntityMap;

public class ArrayTest1 extends JEFEntityMap {
  private List<String[]> stringArray;
  private Integer[]      intArray;

  @Override
  public String toString () {
    return "ArrayTest [stringArray=" + stringArray + ", intArray="
        + Arrays.toString(intArray) + "]";
  }
}
