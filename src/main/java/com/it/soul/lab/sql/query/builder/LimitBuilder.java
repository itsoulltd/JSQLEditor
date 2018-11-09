package com.it.soul.lab.sql.query.builder;

public interface LimitBuilder extends QueryBuilder {
	public QueryBuilder addLimit(Integer limit, Integer offset);
}
