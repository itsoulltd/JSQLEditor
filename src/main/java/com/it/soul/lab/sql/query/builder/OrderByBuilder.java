package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.models.Operator;

public interface OrderByBuilder extends LimitBuilder {
	LimitBuilder orderBy(String...columns);
	LimitBuilder orderBy(Operator order, String...columns);
}
