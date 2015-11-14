package com.jeffreymanzione.jef.test.entities;

import java.util.Arrays;
import java.util.Map;

import com.jeffreymanzione.jef.resurrection.JEFEntityMap;

public class ArrayTest4 extends JEFEntityMap {
  private Map<String, String>[] stringArray;
  private Integer[]             intArray;

  @Override
  public String toString () {
    return "ArrayTest [stringArray=" + stringArray + ", intArray="
        + Arrays.toString(intArray) + "]";
  }
}
