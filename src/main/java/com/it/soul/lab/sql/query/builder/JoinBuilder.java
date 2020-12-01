package com.it.soul.lab.sql.query.builder;

public interface JoinBuilder extends QueryBuilder {
	JoinOnBuilder join(String table, String...columns);
	JoinOnBuilder joinAsAlice(String table, String alice, String...columns);
}
