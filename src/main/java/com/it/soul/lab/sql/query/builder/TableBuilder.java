package com.it.soul.lab.sql.query.builder;

public interface TableBuilder extends QueryBuilder{
	WhereExpressionBuilder from(String name);
	ScalarExpressionBuilder on(String name);
}