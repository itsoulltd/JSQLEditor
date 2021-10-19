package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;
import com.it.soul.lab.sql.query.models.Logic;

import java.util.function.Supplier;

public interface WhereExpressionBuilder extends GroupByBuilder{
	GroupByBuilder where(Logic logic, String... name);
	GroupByBuilder where(Logic logic, Expression... comps);
	GroupByBuilder where(ExpressionInterpreter expression);
	GroupByBuilder where(Supplier<ExpressionInterpreter> supplier);
}