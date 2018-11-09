package com.it.soul.lab.sql.query.models;

public interface ExpressionInterpreter {
	public String interpret();
	public Expression[] resolveExpressions();
}
