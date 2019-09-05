package com.it.soul.lab.jpql.query;

import com.it.soul.lab.sql.query.SQLDeleteQuery;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;

import java.util.List;

public class JPQLDeleteQuery extends SQLDeleteQuery {

    @Override
    protected void prepareTableName(String name) {
        pqlBuffer.append(name + " " + QUIENTIFIER + " ");
    }

    @Override
    protected void prepareWhereParams(List<Expression> whereParams) {
        if(whereParams != null
                && whereParams.size() > 0
                && !isAllParamEmpty(whereParams.toArray())) {

            if(pqlBuffer.length() > 0){
                pqlBuffer.append("WHERE ");
                int count = 0;
                for(Expression param : whereParams){
                    if(param.getProperty().trim().equals("")){continue;}
                    if(count++ != 0){pqlBuffer.append( " " + getLogic().name() + " ");}
                    pqlBuffer.append( QUIENTIFIER + "." + param.getProperty() + " " + param.getType().toString() + " " + ":" + param.getProperty());
                }
            }
        }
    }

    @Override
    protected void prepareWhereExpression(ExpressionInterpreter whereExpression) {
        Expression[] resolved = whereExpression.resolveExpressions();
        for (Expression comp : resolved) {
            comp.setQuientifier(QUIENTIFIER).setMarker(":"+comp.getProperty());
        }
        pqlBuffer.append("WHERE " + whereExpression.interpret());
        for (Expression comp : resolved) {
            comp.setQuientifier(' ');
        }
    }
}
