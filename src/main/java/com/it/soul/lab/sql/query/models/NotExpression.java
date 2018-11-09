package com.it.soul.lab.sql.query.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NotExpression implements ExpressionInterpreter{

	protected ExpressionInterpreter lhr;
	
	public NotExpression(ExpressionInterpreter expression) {
		lhr = expression;
	}
	
	@Override
	public String interpret() {
		return " NOT " + lhr.interpret();
	}

	@Override
	public Expression[] resolveExpressions() {
		List<Expression> comps = new ArrayList<Expression>();
		comps.addAll(Arrays.asList(lhr.resolveExpressions()));
		return comps.toArray(new Expression[0]);
	}
}
