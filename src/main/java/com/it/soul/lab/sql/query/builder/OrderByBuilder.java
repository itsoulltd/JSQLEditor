package com.it.soul.lab.sql.query.builder;

public interface OrderByBuilder extends LimitBuilder {
	public LimitBuilder orderBy(String...columns);
}
