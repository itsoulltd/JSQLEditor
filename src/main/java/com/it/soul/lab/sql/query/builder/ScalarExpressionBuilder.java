package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.Property;

public interface ScalarExpressionBuilder extends WhereExpressionBuilder {
	public QueryBuilder where(Property prop, Expression comps);
}