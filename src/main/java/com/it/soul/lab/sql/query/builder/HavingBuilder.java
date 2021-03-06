package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.models.ExpressionInterpreter;

public interface HavingBuilder extends OrderByBuilder{
	OrderByBuilder having(ExpressionInterpreter expression);
}
