package com.jeffreymanzione.jef.test.entities;

import com.jeffreymanzione.jef.classes.JEFClass;
import com.jeffreymanzione.jef.classes.JEFEntityTuple;
import com.jeffreymanzione.jef.classes.JEFTuple;
import com.jeffreymanzione.jef.classes.StructureType;

@JEFClass(name="TUPL1", type=StructureType.TUPLE)
public class Tuple1 extends JEFEntityTuple {

	@Override
	public int size() {
		return 3;
	}
	
	@JEFTuple(index=0)
	Doge name;

	@JEFTuple(index=1)
	int x;

	@JEFTuple(index=2)
	int y;

	@Override
	public String toString() {
		return "Tuple1 [name=" + name + ", x=" + x + ", y=" + y + "]";
	}

}
