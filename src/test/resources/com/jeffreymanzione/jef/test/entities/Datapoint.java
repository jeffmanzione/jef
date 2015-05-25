package com.jeffreymanzione.jef.test.entities;

import java.util.List;

import com.jeffreymanzione.jef.classes.JEFClass;
import com.jeffreymanzione.jef.classes.JEFEntityMap;

@JEFClass(name="DATAPOINT")
public class Datapoint extends JEFEntityMap {
	int x, y;
	
	String name, desc;
	
	List<Event> events;
}
