package com.it.soul.lab.jpql.query;

import java.util.List;

import com.it.soul.lab.connect.DriverClass;
import com.it.soul.lab.sql.query.SQLSelectQuery;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;
import com.it.soul.lab.sql.query.models.Operator;

public class JPQLSelectQuery extends SQLSelectQuery {

	@Override
	protected String queryString(DriverClass dialect) throws IllegalArgumentException {
		if(getTableName() == null || getTableName().trim().equals("")){
			throw new IllegalArgumentException("Parameter Table must not be Null OR Empty.");
		}
		return pqlBuffer.toString();
	}
	
	@Override
	protected void prepareTableName(String name) {
		pqlBuffer.append(" FROM "+ name + " " + QUANTIFIER);
	}
	
	@Override
	protected void prepareColumns(String[] columns) {
		if(getColumns() != null && getColumns().length > 0){

			int count = 0;
			for(String str : getColumns()){
				if(str.trim().equals("")){continue;}
				if(count++ != 0){pqlBuffer.append(", ");}
				pqlBuffer.append( QUANTIFIER + "." + str);
			}
			//If all passed parameter is empty
			if(count == 0){pqlBuffer.append(QUANTIFIER);}
		}else{
			pqlBuffer.append(QUANTIFIER);
		}
	}
	
	@Override
	protected void prepareWhereParams(String[] whereParams) {
		prepareWhereParams(Expression.createListFrom(whereParams, Operator.EQUAL));
	}
	
	@Override
	protected void prepareWhereParams(List<Expression> whereParams) {
		if (whereParams != null 
				&& whereParams.size() > 0
				&& !isAllParamEmpty(whereParams.toArray())){
			
			if(pqlBuffer.length() > 0){
				pqlBuffer.append(" WHERE ");
				int count = 0;
				for( Expression param : whereParams ){
					if(param.getProperty().trim().equals("")){continue;}
					if(count++ != 0){pqlBuffer.append( " " + getLogic().name() + " ");}
					pqlBuffer.append(QUANTIFIER + "." + param.getProperty()+ " " + param.getType().toString() + " :" + param.getProperty());
				}
			}
		}
	}
	
	@Override
	protected void prepareWhereExpression(ExpressionInterpreter whereExpression) {
		Expression[] resolved = whereExpression.resolveExpressions();
		for (Expression comp : resolved) {
			comp.setQuantifier(QUANTIFIER).setMarker(":"+comp.getProperty());
		}
		pqlBuffer.append(" WHERE " + whereExpression.interpret());
		for (Expression comp : resolved) {
			comp.setQuantifier(' ');
		}
	}
	
	@Override
	public void setOrderBy(List<String> columns, Operator opt) {
		setQuantifier(QUANTIFIER);
		super.setOrderBy(columns, opt);
		setQuantifier(' ');
	}
	
	@Override
	public void setGroupBy(List<String> columns) {
		setQuantifier(QUANTIFIER);
		super.setGroupBy(columns);
		setQuantifier(' ');
	}
	
}
