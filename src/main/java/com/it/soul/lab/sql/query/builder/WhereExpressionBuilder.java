package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.models.Logic;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;

public interface WhereExpressionBuilder extends GroupByBuilder{
	GroupByBuilder where(Logic logic, String... name);
	GroupByBuilder where(Logic logic, Expression... comps);
	GroupByBuilder where(ExpressionInterpreter expression);
}