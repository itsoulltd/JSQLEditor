package com.it.soul.lab.sql.query.models;

public interface Predicate extends ExpressionInterpreter {
	public Predicate and(ExpressionInterpreter exp);
	public Predicate or(ExpressionInterpreter exp);
	public Predicate not();
	public WhereClause and(String key);
	public WhereClause or(String key);
}
