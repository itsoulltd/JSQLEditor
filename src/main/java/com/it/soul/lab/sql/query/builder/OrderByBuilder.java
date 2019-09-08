package com.it.soul.lab.sql.query.builder;

public interface OrderByBuilder extends LimitBuilder {
	LimitBuilder orderBy(String...columns);
}
