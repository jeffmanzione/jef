package com.jeffreymanzione.jef.classes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JEFField {
	String key() default "";
	boolean ignore() default false;
}
