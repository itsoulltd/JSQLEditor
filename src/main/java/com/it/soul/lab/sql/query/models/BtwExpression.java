package com.it.soul.lab.sql.query.models;

import java.util.List;

public class BtwExpression extends Expression{

    public BtwExpression(Property property, Operator type) {
        super(property, type);
    }

    public BtwExpression(String property, Operator type) {
        super(property, type);
    }

    @Override
    public String interpret() {
        if (getValueProperty().getType() == DataType.LIST
                && getValueProperty().getValue() != null) {
            List items = (List) getValueProperty().getValue();
            if (items.size() == 2) { //Must be 2 value
                if (Character.isWhitespace(getQuantifier()) == false){
                    return getQuantifier()+ "." + getProperty() + " " + getType().toString() + " " + getExpressMarker() + " AND " + getExpressMarker() + "";
                } else {
                    return getProperty() + " " + getType().toString() + " " + getMARKER() + " AND " + getMARKER() + "";
                }
            }
        }
        return super.interpret();
    }
}
