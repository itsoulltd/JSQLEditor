package com.it.soul.lab.cql.query;

import com.it.soul.lab.sql.query.SQLDeleteQuery;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;

public class CQLDeleteQuery extends SQLDeleteQuery {
    @Override
    protected void prepareWhereExpression(ExpressionInterpreter whereExpression) {
        //FIXME:
        String clause = whereExpression.interpret();
        clause = clause.replace("(", "");
        clause = clause.replace(")", "");
        pqlBuffer.append(" WHERE " + clause);
    }
}
