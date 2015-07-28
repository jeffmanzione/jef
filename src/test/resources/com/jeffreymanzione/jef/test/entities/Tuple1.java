package com.jeffreymanzione.jef.test.entities;

import com.jeffreymanzione.jef.resurrection.JEFEntityTuple;
import com.jeffreymanzione.jef.resurrection.annotations.JEFClass;
import com.jeffreymanzione.jef.resurrection.annotations.JEFTuple;
import com.jeffreymanzione.jef.resurrection.annotations.StructureType;

@JEFClass(name="Tupl1")
public class Tuple1 extends JEFEntityTuple {

	@Override
	public int size() {
		return 3;
	}
	
	@JEFTuple(0)
	Doge name;

	@JEFTuple(1)
	int x;

	@JEFTuple(2)
	int y;

	@Override
	public String toString() {
		return "Tuple1 [name=" + name + ", x=" + x + ", y=" + y + "]";
	}

}
