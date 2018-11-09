package com.it.soul.lab.jpql.query;

import java.util.List;

import com.it.soul.lab.sql.query.SQLUpdateQuery;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;
import com.it.soul.lab.sql.query.models.Operator;

public class JPQLUpdateQuery extends SQLUpdateQuery {
	
	@Override
	protected void prepareTableName(String name) {
		pqlBuffer.append(getTableName() + " " + QUIENTIFIER + " SET");
	}
	
	@Override
	protected void prepareColumns(String[] columns) {
		if(getColumns() != null && getColumns().length > 0){
			int count = 0;
			for(String column : getColumns()){
				if(column.trim().equals("")){continue;}
				if(count++ != 0){paramBuffer.append(", ");}
				paramBuffer.append( QUIENTIFIER + "." + column + " = :" + column);
			}
		}
	}
	
	@Override
	protected void prepareWhereParams(String[] whereParams) {
		prepareWhereParams(Expression.createListFrom(whereParams, Operator.EQUAL));
	}
	
	@Override
	protected void prepareWhereParams(List<Expression> whereParams) {
		if(whereParams != null 
				&& whereParams.size() > 0
				&& !isAllParamEmpty(whereParams.toArray())) {
			
			if(whereBuffer.length() > 0){
				whereBuffer.append("WHERE ");
				int count = 0;
				for(Expression param : whereParams){
					if(param.getProperty().trim().equals("")){continue;}
					if(count++ != 0){whereBuffer.append( " " + getLogic().name() + " ");}
					whereBuffer.append( QUIENTIFIER + "." + param.getProperty() + " " + param.getType().toString() + " " + ":" + param.getProperty());
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
		whereBuffer.append("WHERE " + whereExpression.interpret());
		for (Expression comp : resolved) {
			comp.setQuientifier(' ');
		}
	}
}
