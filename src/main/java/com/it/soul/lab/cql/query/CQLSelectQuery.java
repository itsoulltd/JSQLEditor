package com.it.soul.lab.cql.query;

import com.it.soul.lab.connect.DriverClass;
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

    @Override
    protected void appendLimit(StringBuffer pqlBuffer, DriverClass dialect) {
        //super.appendLimit(pqlBuffer, dialect);
        if (limit > 0) {
            if (pqlBuffer.toString().contains("LIMIT"))
                return;
            if (pqlBuffer.toString().endsWith("ALLOW FILTERING")) {
                int start = pqlBuffer.length() - "ALLOW FILTERING".length() - 1;
                int end = pqlBuffer.length();
                pqlBuffer.replace(start, end, " LIMIT " + limit + " ALLOW FILTERING");
                return;
            }
            if(!pqlBuffer.toString().endsWith("ALLOW FILTERING")) {
                pqlBuffer.append(" LIMIT " + limit + " ALLOW FILTERING");
            }
        }
    }
}
