package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.Property;

public interface ScalerClauseBuilder extends WhereClauseBuilder{
	public QueryBuilder scalerClause(Property prop, Expression comps);
}