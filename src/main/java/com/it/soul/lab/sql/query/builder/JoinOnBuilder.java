package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.models.JoinExpression;

public interface JoinOnBuilder extends OrderByBuilder{
	public JoinBuilder on(JoinExpression expression);
}
