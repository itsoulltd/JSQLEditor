package com.it.soul.lab.sql.query.builder;



public interface TableBuilder extends QueryBuilder{
	public WhereExpressionBuilder from(String name);
	public ScalarExpressionBuilder on(String name);
}