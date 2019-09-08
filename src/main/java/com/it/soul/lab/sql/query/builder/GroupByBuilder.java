package com.it.soul.lab.sql.query.builder;

public interface GroupByBuilder extends OrderByBuilder{
	HavingBuilder groupBy(String...columns);
}
