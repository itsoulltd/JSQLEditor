package com.it.soul.lab.sql.query.models;

import java.util.List;

public class InExpression extends Expression {
    public InExpression(Property property, Operator type) {
        super(property, type);
    }

    public InExpression(String property, Operator type) {
        super(property, type);
    }

    @Override
    public String interpret() {
        if (getValueProperty().getType() == DataType.LIST
                && getValueProperty().getValue() != null){
            List items = (List) getValueProperty().getValue();
            if (items.size() > 0) {
                StringBuffer buffer = new StringBuffer();
                if (Character.isWhitespace(getQuantifier()) == false){
                    buffer.append( " " + getExpressMarker());
                    return getQuantifier()+ "." + getProperty() + " " + getType().toString() + " (" + buffer.toString().replaceFirst(",", "") + " )";
                }
                else {
                    //items.forEach(o -> buffer.append(", " + getMARKER()));
                    buffer.append(" " + getMARKER());
                    return getProperty() + " " + getType().toString() + " (" + buffer.toString().replaceFirst(",", "") + " )";
                }
            }
        }
        return super.interpret();
    }
}
