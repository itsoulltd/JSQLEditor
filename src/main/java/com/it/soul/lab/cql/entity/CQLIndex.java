package com.it.soul.lab.cql.entity;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Inherited
@Retention(RUNTIME)
@Target(FIELD)
public @interface CQLIndex {
    String prefix() default "idx";
    String name() default "";
    boolean custom() default false;
    String using() default "";
    String options() default "";
}
