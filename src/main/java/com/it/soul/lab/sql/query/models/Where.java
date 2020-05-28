package com.it.soul.lab.sql.query.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Where implements WhereClause {

    public Where(String key) {
        _proxy = new PredicateProxy(key);
    }

    private PredicateProxy _proxy;

    private PredicateProxy getProxy() {
        return _proxy;
    }

    private class PredicateProxy implements Predicate {

        private String key;
        private Logic logic = Logic.AND;
        private ExpressionInterpreter expression;

        public PredicateProxy(String key) {
            this.key = key;
        }

        @Override
        public String interpret() {
            return expression.interpret();
        }

        @Override
        public Expression[] resolveExpressions() {
            return expression.resolveExpressions();
        }

        private Predicate create(ExpressionInterpreter exp) {
            if (expression == null) {
                expression = exp;
            } else {
                if (logic == Logic.AND) {
                    createAnd(exp);
                } else {
                    createOr(exp);
                }
            }
            return this;
        }

        private Predicate createExpression(Object value, Operator opt) {
            ExpressionInterpreter exp = new Expression(new Property(key, value), opt);
            return create(exp);
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

        private Predicate createIn(Object[] value, Operator opt) {
            ExpressionInterpreter exp = new InExpression(new Property(key, Arrays.asList(value)), opt);
            return create(exp);
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
        return getProxy().createExpression(value, Operator.EQUAL);
    }

    @Override
    public Predicate isGreaterThen(Object value) {
        return getProxy().createExpression(value, Operator.GREATER_THAN);
    }

    @Override
    public Predicate notEqualTo(Object value) {
        return getProxy().createExpression(value, Operator.NOTEQUAL);
    }

    @Override
    public Predicate isGreaterThenOrEqual(Object value) {
        return getProxy().createExpression(value, Operator.GREATER_THAN_OR_EQUAL);
    }

    @Override
    public Predicate isLessThen(Object value) {
        return getProxy().createExpression(value, Operator.LESS_THAN);
    }

    @Override
    public Predicate isLessThenOrEqual(Object value) {
        return getProxy().createExpression(value, Operator.LESS_THAN_OR_EQUAL);
    }

    @Override
    public Predicate isIn(Object... value) {
        List<Object> filtered = new ArrayList();
        if (value.length > 0) {
            for (Object o : value) {
                if (o instanceof List) {
                    filtered.add(o);
                }
            }
        }
        value = (filtered.size() > 0) ? filtered.toArray() : value;
        return getProxy().createIn(value, Operator.IN);
    }

    @Override
    public Predicate notIn(Object... value) {
        List<Object> filtered = new ArrayList();
        if (value.length > 0) {
            for (Object o : value) {
                if (o instanceof List) {
                    filtered.add(o);
                }
            }
        }
        value = (filtered.size() > 0) ? filtered.toArray() : value;
        return getProxy().createIn(value, Operator.NOT_IN);
    }

    @Override
    public Predicate isLike(Object value) {
        return getProxy().createExpression(value, Operator.LIKE);
    }

    @Override
    public Predicate notLike(Object value) {
        return getProxy().createExpression(value, Operator.NOT_LIKE);
    }

    @Override
    public Predicate isNull() {
        ExpressionInterpreter exp = new Expression(new Property(getProxy().key, null, DataType.NULL_SKIP), Operator.IS_NULL);
        return getProxy().create(exp);
    }

    @Override
    public Predicate notNull() {
        ExpressionInterpreter exp = new Expression(new Property(getProxy().key, null, DataType.NULL_SKIP), Operator.NOT_NULL);
        return getProxy().create(exp);
    }
}
