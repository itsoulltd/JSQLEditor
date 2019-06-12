package com.it.soul.lab.sql.query.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndExpression implements ExpressionInterpreter {

	protected ExpressionInterpreter lhr;
	protected ExpressionInterpreter rhr;

	protected char leftParenthesis = '(';
	protected char rightParenthesis = ')';

	public void setParenthesis(char left, char right){
		leftParenthesis = left;
		rightParenthesis = right;
		if (lhr != null && lhr instanceof AndExpression){
			((AndExpression)lhr).setParenthesis(left, right);
		}
		if (rhr != null && rhr instanceof AndExpression){
			((AndExpression)rhr).setParenthesis(left, right);
		}
	}

	public void disableParenthesis(){
		this.setParenthesis(' ', ' ');
	}

	public void enableParenthesis(){
		this.setParenthesis(leftParenthesis, rightParenthesis);
	}

	protected boolean skipParenthesis(){
		return Character.isWhitespace(leftParenthesis) && Character.isWhitespace(rightParenthesis);
	}
	
	public AndExpression(ExpressionInterpreter lhr, ExpressionInterpreter rhr) {
		this.lhr = lhr;
		this.rhr = rhr;
	}

	@Override
	public String interpret() {
		if (skipParenthesis() == false) return leftParenthesis + " " + lhr.interpret() + " AND " + rhr.interpret() + " " + rightParenthesis;
		else return lhr.interpret() + " AND " + rhr.interpret();
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
