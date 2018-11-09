package com.it.soul.lab.sql.query.builder;

public interface JoinBuilder extends QueryBuilder {
	public JoinOnBuilder join(String table, String...columns);
}
