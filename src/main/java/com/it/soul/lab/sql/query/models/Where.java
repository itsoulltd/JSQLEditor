package com.it.soul.lab.sql.query.models;

public class Where implements WhereClause {
	
	public Where(String key) {
		_proxy = new PredicateProxy(key);
	}
	
	private PredicateProxy _proxy;
	private PredicateProxy getProxy() {
		return _proxy;
	}

	private class PredicateProxy implements Predicate{
		
		private String key;
		private Logic logic = Logic.AND;
		private ExpressionInterpreter expression;

		public PredicateProxy(String key) {
			this.key = key;
		}
		
		public Predicate createExpression(Object value, DataType type, Operator opt) {
			if (type == null) {
				type = DataType.getDataType(value);
			}
			ExpressionInterpreter exp = new Expression(new Property(key, value, type), opt);
			if(expression == null) {
				expression = exp;
			}else {
				if(logic == Logic.AND) { createAnd(exp);}
				else {createOr(exp);}
			}
			return this;
		}

		@Override
		public String interpret() {
			return expression.interpret();
		}

		@Override
		public Expression[] resolveExpressions() {
			return expression.resolveExpressions();
		}
		
		private void createAnd(ExpressionInterpreter exp) {
			expression = new AndExpression(expression, exp);
		}
		
		private void createOr(ExpressionInterpreter exp) {
			expression = new OrExpression(expression, exp);
		}
		
		private void createNor() {
			expression = new NotExpression(expression);
		}
		
		@Override
		public Predicate and(ExpressionInterpreter exp) {
			createAnd(exp);
			return this;
		}

		@Override
		public Predicate or(ExpressionInterpreter exp) {
			createOr(exp);
			return this;
		}
		
		@Override
		public Predicate not() {
			createNor();
			return this;
		}
		
		@Override
		public WhereClause and(String key) {
			this.key = key;
			this.logic = Logic.AND;
			return Where.this;
		}

		@Override
		public WhereClause or(String key) {
			this.key = key;
			this.logic = Logic.OR;
			return Where.this;
		}
		
	}
	
	@Override
	public Predicate isEqualTo(Object value) {
		return getProxy().createExpression(value, null, Operator.EQUAL);
	}

	@Override
	public Predicate isGreaterThen(Object value) {
		return getProxy().createExpression(value, null, Operator.GREATER_THAN);
	}

	@Override
	public Predicate notEqualTo(Object value) {
		return getProxy().createExpression(value, null, Operator.NOTEQUAL);
	}

	@Override
	public Predicate isGreaterThenOrEqual(Object value) {
		return getProxy().createExpression(value, null, Operator.GREATER_THAN_OR_EQUAL);
	}

	@Override
	public Predicate isLessThen(Object value) {
		return getProxy().createExpression(value, null, Operator.LESS_THAN);
	}

	@Override
	public Predicate isLessThenOrEqual(Object value) {
		return getProxy().createExpression(value, null, Operator.LESS_THAN_OR_EQUAL);
	}

	@Override
	public Predicate isIn(Object value) {
		return getProxy().createExpression(value, null, Operator.IN);
	}

	@Override
	public Predicate notIn(Object value) {
		return getProxy().createExpression(value, null, Operator.NOT_IN);
	}

	@Override
	public Predicate isLike(Object value) {
		return getProxy().createExpression(value, null, Operator.LIKE);
	}

	@Override
	public Predicate notLike(Object value) {
		return getProxy().createExpression(value, null, Operator.NOT_LIKE);
	}

	
}
