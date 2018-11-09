package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.models.Logic;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;

public interface WhereClauseBuilder extends GroupByBuilder{
	public GroupByBuilder whereParams(Logic logic, String... name);
	public GroupByBuilder whereParams(Logic logic, Expression... comps);
	public GroupByBuilder where(ExpressionInterpreter expression);
}