package com.it.soul.lab.sql.entity;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.it.soul.lab.sql.query.models.DataType;

@Retention(RUNTIME)
@Target(FIELD)
public @interface Column {
	String name() default "";
	String defaultValue() default "";
	DataType type() default DataType.STRING;
	String parseFormat() default "";
}
