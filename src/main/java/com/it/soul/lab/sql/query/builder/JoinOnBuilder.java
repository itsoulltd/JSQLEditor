package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.models.JoinExpression;

public interface JoinOnBuilder extends WhereExpressionBuilder{
	JoinBuilder on(JoinExpression expression);
	JoinBuilder on(String leftColumn, String rightColumn);
	JoinOnBuilder rejoin(String table);
	JoinOnBuilder rejoin(Class<? extends Entity> cType);
}
