package com.jeffreymanzione.jef.resurrection.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface JEFTuple {
	int value();
}
