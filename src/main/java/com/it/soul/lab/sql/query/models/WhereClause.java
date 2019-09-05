package com.it.soul.lab.sql.query.models;

public interface WhereClause {
	Predicate isEqualTo(Object value);
	Predicate notEqualTo(Object value);
	Predicate isGreaterThen(Object value);
	Predicate isGreaterThenOrEqual(Object value);
	Predicate isLessThen(Object value);
	Predicate isLessThenOrEqual(Object value);
	Predicate isIn(Object value);
	Predicate notIn(Object value);
	Predicate isLike(Object value);
	Predicate notLike(Object value);
	Predicate isNull();
    Predicate notNull();
}
