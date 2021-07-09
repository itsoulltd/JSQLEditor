package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.entity.Entity;

public interface TableBuilder extends QueryBuilder{
	WhereExpressionBuilder from(String name);
	WhereExpressionBuilder from(Class<? extends Entity> cType);
	ScalarExpressionBuilder on(String name);
	ScalarExpressionBuilder on(Class<? extends Entity> cType);
}