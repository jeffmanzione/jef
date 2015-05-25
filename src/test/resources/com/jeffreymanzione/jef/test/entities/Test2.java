package com.jeffreymanzione.jef.test.entities;

import java.util.List;

import com.jeffreymanzione.jef.classes.JEFEntityMap;
import com.jeffreymanzione.jef.classes.JEFClass;
import com.jeffreymanzione.jef.classes.JEFField;


@JEFClass(name = "TEST2")
public class Test2 extends JEFEntityMap {
	
	@JEFField(key="name")
	private String nombre;
	
	private int x;
	private int y;
	
	private Test1 a;
	
	private List<Test1> test1List;

	private Doge doge;
	
	@JEFField(ignore=true)
	private Doge secret = Doge.a;

	@Override
	public String toString() {
		return "Test2 [nombre=" + nombre + ", x=" + x + ", y=" + y + ", a=" + a + ", test1List=" + test1List
				+ ", doge=" + doge + "]";
	}
	
}
