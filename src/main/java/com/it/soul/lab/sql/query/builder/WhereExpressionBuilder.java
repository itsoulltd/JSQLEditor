package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.models.Logic;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;

public interface WhereExpressionBuilder extends GroupByBuilder{
	public GroupByBuilder where(Logic logic, String... name);
	public GroupByBuilder where(Logic logic, Expression... comps);
	public GroupByBuilder where(ExpressionInterpreter expression);
}