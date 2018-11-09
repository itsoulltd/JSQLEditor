package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.models.Property;



public interface ColumnsBuilder extends QueryBuilder{
	public TableBuilder columns(String... name);
	public InsertBuilder into(String name);
	public WhereClauseBuilder rowsFrom(String name);
	public TableBuilder set(Property... properties);
}