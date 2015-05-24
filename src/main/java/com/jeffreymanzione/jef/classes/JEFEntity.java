package com.jeffreymanzione.jef.classes;

public interface JEFEntity {

	boolean addToMap(String fieldName, Object val) throws CouldNotUpdateEntityMapException;

	Object getFromMap(String fieldName) throws CouldNotUpdateEntityMapException;

	Class<?> getFromMapType(String fieldName) throws CouldNotUpdateEntityMapException;
}
