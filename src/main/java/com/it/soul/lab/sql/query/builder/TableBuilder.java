package com.it.soul.lab.sql.query.builder;



public interface TableBuilder extends QueryBuilder{
	public WhereClauseBuilder from(String name);
	public ScalerClauseBuilder on(String name);
}