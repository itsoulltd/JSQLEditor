package com.it.soul.lab.cql.query;

import com.it.soul.lab.sql.query.SQLSelectQuery;
import com.it.soul.lab.sql.query.models.AndExpression;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;

public class CQLSelectQuery extends SQLSelectQuery {

    @Override
    protected void prepareWhereExpression(ExpressionInterpreter whereExpression) {
        //FIXME:
        String clause = whereExpression.interpret();
        clause = clause.replace("(", "");
        clause = clause.replace(")", "");
        pqlBuffer.append(" WHERE " + clause);
    }
}
