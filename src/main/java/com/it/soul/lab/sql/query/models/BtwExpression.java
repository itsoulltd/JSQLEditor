package com.it.soul.lab.sql.query.models;

import java.util.List;

public class BtwExpression extends Expression {

    private Property secondValueProperty;

    public Property getSecondValueProperty() {
        return secondValueProperty;
    }

    public BtwExpression(Property first, Property second, Operator type) {
        super(first, type);
        this.secondValueProperty = second;
    }

    @Override
    public String interpret() {
        if (getValueProperty() != null && getSecondValueProperty() != null) {
            if (Character.isWhitespace(getQuantifier()) == false){
                return getQuantifier()+ "." + getProperty() + " " + getType().toString() + " " + getExpressMarker() + " AND " + getExpressMarker() + "";
            } else {
                return getProperty() + " " + getType().toString() + " " + getMARKER() + " AND " + getMARKER() + "";
            }
        }
        return super.interpret();
    }

    @Override
    public Expression[] resolveExpressions() {
        return new Expression[] {new Expression(getValueProperty(), getType())
                , new Expression(getSecondValueProperty(), getType())};
    }

}
