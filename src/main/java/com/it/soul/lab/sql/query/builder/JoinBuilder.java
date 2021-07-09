package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.entity.Entity;

public interface JoinBuilder extends QueryBuilder {
	JoinOnBuilder join(String table, String...columns);
	JoinOnBuilder join(Class<? extends Entity> cType, String...columns);
	JoinOnBuilder joinAsAlice(String table, String alice, String...columns);
	JoinOnBuilder joinAsAlice(Class<? extends Entity> cType, String alice, String...columns);
}
