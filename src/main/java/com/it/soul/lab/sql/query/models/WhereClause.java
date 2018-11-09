package com.it.soul.lab.sql.query.models;

public interface WhereClause {
	public Predicate isEqualTo(Object value);
	public Predicate notEqualTo(Object value);
	public Predicate isGreaterThen(Object value);
	public Predicate isGreaterThenOrEqual(Object value);
	public Predicate isLessThen(Object value);
	public Predicate isLessThenOrEqual(Object value);
	public Predicate isIn(Object value);
	public Predicate notIn(Object value);
	public Predicate isLike(Object value);
	public Predicate notLike(Object value);
}
