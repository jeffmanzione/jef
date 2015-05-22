package com.jeffreymanzione.jef.classes;

public interface JEFEntity {

	boolean addToMap(String fieldName, Object val);

	Object getFromMap(String fieldName) throws IllegalArgumentException, IllegalAccessException;

	Class<?> getFromMapType(String fieldName);
}
