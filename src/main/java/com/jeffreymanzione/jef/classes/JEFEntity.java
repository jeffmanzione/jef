package com.jeffreymanzione.jef.classes;

public interface JEFEntity {

	public boolean addToMap(String fieldName, Object val);

	public Object getFromMap(String fieldName) throws IllegalArgumentException, IllegalAccessException;

	public Class<?> getFromMapType(String fieldName);
}
