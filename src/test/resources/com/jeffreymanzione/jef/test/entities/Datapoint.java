package com.jeffreymanzione.jef.test.entities;

import java.util.List;

import com.jeffreymanzione.jef.resurrection.JEFEntityMap;
import com.jeffreymanzione.jef.resurrection.annotations.JEFClass;

@JEFClass(name="Datapoint")
public class Datapoint extends JEFEntityMap {
	int x, y;
	
	String name, desc;
	
	List<Event> events;
}
