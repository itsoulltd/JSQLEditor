package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.SQLQuery;

public interface QueryBuilder{
	<T extends SQLQuery> T build();
}