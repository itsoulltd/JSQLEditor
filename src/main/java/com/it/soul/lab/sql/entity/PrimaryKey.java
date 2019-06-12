package com.it.soul.lab.sql.entity;

import com.it.soul.lab.sql.query.models.DataType;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface PrimaryKey {
	String name();
	DataType type() default DataType.STRING;
	boolean autoIncrement() default false;
}
