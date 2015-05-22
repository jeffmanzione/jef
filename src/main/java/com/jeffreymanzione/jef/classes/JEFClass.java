package com.jeffreymanzione.jef.classes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface JEFClass {
	String name();
	StructureType type() default StructureType.MAP;
}
