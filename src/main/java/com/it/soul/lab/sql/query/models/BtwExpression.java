package com.it.soul.lab.sql.query.models;

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
            if (Character.isWhitespace(getQuantifier()) == false) {
                return getQuantifier()+ "." + getProperty() + " " + getType().toString() + String.format(" %s_%s", getExpressMarker(), "left") + " AND " + String.format("%s_%s", getExpressMarker(), "right") + "";
            } else {
                return getProperty() + " " + getType().toString() + " " + getMARKER() + " AND " + getMARKER() + "";
            }
        }
        return super.interpret();
    }

    @Override
    public Expression[] resolveExpressions() {
        return new Expression[] {this, new Expression(getSecondValueProperty(), getType())};
    }

}
