package com.jeffreymanzione.jef.classes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JEF {
	String key();
	boolean ignore();
}
