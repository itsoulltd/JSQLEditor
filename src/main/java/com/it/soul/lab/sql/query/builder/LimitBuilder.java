package com.it.soul.lab.sql.query.builder;

public interface LimitBuilder extends QueryBuilder {
	QueryBuilder addLimit(Integer limit, Integer offset);
}
