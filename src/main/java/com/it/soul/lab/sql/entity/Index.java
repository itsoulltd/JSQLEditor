package com.it.soul.lab.sql.entity;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@Retention(RUNTIME)
@Target(TYPE)
public @interface Index {
    String prefix() default "idx";
    String name() default "";
    boolean unique() default false;
    String[] columns() default {};
}
