package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.models.Property;

public interface ColumnsBuilder extends QueryBuilder{
	TableBuilder columns(String... name);
	InsertBuilder into(String name);
	InsertBuilder into(Class<? extends Entity> cType);
	WhereExpressionBuilder rowsFrom(String name);
	WhereExpressionBuilder rowsFrom(Class<? extends Entity> cType);
	TableBuilder set(Property... properties);
}