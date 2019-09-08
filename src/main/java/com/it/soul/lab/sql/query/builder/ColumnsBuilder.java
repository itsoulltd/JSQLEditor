package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.models.Property;

public interface ColumnsBuilder extends QueryBuilder{
	TableBuilder columns(String... name);
	InsertBuilder into(String name);
	WhereExpressionBuilder rowsFrom(String name);
	TableBuilder set(Property... properties);
}