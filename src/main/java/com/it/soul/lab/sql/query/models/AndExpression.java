package com.it.soul.lab.sql.query.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndExpression implements ExpressionInterpreter {

	protected ExpressionInterpreter lhr;
	protected ExpressionInterpreter rhr;
	
	public AndExpression(ExpressionInterpreter lhr, ExpressionInterpreter rhr) {
		this.lhr = lhr;
		this.rhr = rhr;
	}

	@Override
	public String interpret() {
		return "( " + lhr.interpret() + " AND " + rhr.interpret() + " )";
	}
	
	@Override
	public String toString() {
		return interpret();
	}
	
	@Override
	public Expression[] resolveExpressions() {
		List<Expression> comps = new ArrayList<Expression>();
		comps.addAll(Arrays.asList(lhr.resolveExpressions()));
		comps.addAll(Arrays.asList(rhr.resolveExpressions()));
		return comps.toArray(new Expression[0]);
	}

}
