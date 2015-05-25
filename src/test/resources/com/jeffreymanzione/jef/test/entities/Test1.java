package com.jeffreymanzione.jef.test.entities;

import com.jeffreymanzione.jef.classes.JEFEntityMap;
import com.jeffreymanzione.jef.classes.JEFClass;

@JEFClass(name = "TEST1")
public class Test1 extends JEFEntityMap {
	private int int1;
	private double float1;
	private String string1;
	private Tuple1 tup;
	
	@Override
	public String toString() {
		return "Test1 [int1=" + int1 + ", float1=" + float1 + ", string1=" + string1 + ", tup=" + tup + "]";
	}
	
	
}
