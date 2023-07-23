package com.it.soul.lab.sql.query.models;

@FunctionalInterface
public interface WherePredicate {
    Predicate apply(Property nextPagingKey);
}
