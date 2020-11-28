package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.models.JoinExpression;

public interface JoinOnBuilder extends WhereExpressionBuilder{
	JoinBuilder on(JoinExpression expression);
	JoinOnBuilder rejoin(String table);
}
