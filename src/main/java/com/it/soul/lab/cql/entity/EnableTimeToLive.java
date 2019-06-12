package com.it.soul.lab.cql.entity;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableTimeToLive {
    long value() default 60 * 60 * 24;
}
