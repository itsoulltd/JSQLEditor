package com.it.soul.lab.cql.query;

import com.it.soul.lab.sql.query.SQLUpdateQuery;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;

public class CQLUpdateQuery extends SQLUpdateQuery {
    @Override
    protected void prepareWhereExpression(ExpressionInterpreter whereExpression) {
        //FIXME:
        String clause = whereExpression.interpret();
        clause = clause.replace("(", "");
        clause = clause.replace(")", "");
        whereBuffer.append(" WHERE " + clause);
    }
}
