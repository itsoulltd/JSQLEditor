package com.it.soul.lab.sql.query;

import java.util.List;

import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.Logic;
import com.it.soul.lab.sql.query.models.Operator;

public class SQLDeleteQuery extends SQLSelectQuery{
	
	public SQLDeleteQuery() {
		this.pqlBuffer = new StringBuffer("DELETE FROM ");
	}
	
	@Override
	protected String queryString() throws IllegalArgumentException {
		if(getTableName() == null || getTableName().trim().equals("")){
			throw new IllegalArgumentException("Parameter Table must not be Null OR Empty.");
		}
		return pqlBuffer.toString();
	}
	
	@Override
	protected void prepareTableName(String name) {
		pqlBuffer.append(name + " ");
	}
	
	@Override
	public void setColumns(String[] columns) {
		//Skip
	}
	
	@Override
	protected void prepareWhereParams(String[] whereParams) {
		prepareWhereParams(Expression.createListFrom(whereParams, Operator.EQUAL));
	}
	
	@Override
	protected void prepareWhereParams(List<Expression> whereParams) {
		if(whereParams != null 
				&& whereParams.size() > 0
				&& !isAllParamEmpty(whereParams.toArray())){

			if(pqlBuffer.length() > 0){
				pqlBuffer.append( "WHERE ");
				int count = 0;
				for(Expression ent : whereParams){
					if(ent.getProperty().trim().equals("")){continue;}
					if(count++ != 0){pqlBuffer.append( " " + getLogic().name() + " ");}
					pqlBuffer.append( ent.getProperty() + " " + ent.getType().toString() + " " + MARKER);
				}
			}
		}
	}
	
	public static String create(String tableName ,Logic whereLogic ,List<Expression> whereParams)
			throws IllegalArgumentException{

		//Checking Illegal Arguments
		try{
			if(tableName == null || tableName.trim().equals("")){
				throw new IllegalArgumentException("Parameter 'tableName' must not be Null OR Empty.");
			}
		}catch(IllegalArgumentException iex){throw iex;}

		//Query Builders
		StringBuffer pqlBuffer = new StringBuffer("DELETE FROM "+ tableName);
		if(whereParams != null 
				&& whereParams.size() > 0
				&& !isAllParamEmpty(whereParams.toArray())){

			if(pqlBuffer.length() > 0){
				pqlBuffer.append( " WHERE ");
				int count = 0;
				for(Expression ent : whereParams){
					if(ent.getProperty().trim().equals("")){continue;}
					if(count++ != 0){pqlBuffer.append( " " + whereLogic.name() + " ");}
					pqlBuffer.append( ent.getProperty() + " " + ent.getType().toString() +" " + MARKER);
				}
			}
		}
		//
		return pqlBuffer.toString();
	}

}