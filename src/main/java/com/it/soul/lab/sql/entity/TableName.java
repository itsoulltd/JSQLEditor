package com.it.soul.lab.sql.entity;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Inherited
@Retention(RUNTIME)
@Target(TYPE)
public @interface TableName {
	String value() default "";
	boolean acceptAll() default true;
}
