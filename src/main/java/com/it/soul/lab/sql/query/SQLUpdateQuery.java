package com.it.soul.lab.sql.query;

import java.util.List;

import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;
import com.it.soul.lab.sql.query.models.Logic;
import com.it.soul.lab.sql.query.models.Operator;
import com.it.soul.lab.sql.query.models.Row;
import com.it.soul.lab.sql.query.models.Property;

public class SQLUpdateQuery extends SQLSelectQuery{
	
	protected StringBuffer paramBuffer;
	protected StringBuffer whereBuffer;
	private Row row;
	
	public SQLUpdateQuery() {
		this.pqlBuffer = new StringBuffer("UPDATE ");
		this.paramBuffer = new StringBuffer(" ");
		this.whereBuffer = new StringBuffer(" ");
	}
	
	@Override
	protected String queryString() throws IllegalArgumentException {
		if(getTableName() == null || getTableName().trim().equals("")){
			throw new IllegalArgumentException("Parameter Table must not be Null OR Empty.");
		}
		if(isAllParamEmpty(getColumns())){
			throw new IllegalArgumentException("All Empty Parameters!!! You nuts (:D");
		}
		return pqlBuffer.toString() + paramBuffer.toString() + whereBuffer.toString();
	}
	
	@Override
	protected void prepareColumns(String[] columns) {
		if(getColumns() != null && getColumns().length > 0){
			int count = 0;
			for(String column : getColumns()){
				if(column.trim().equals("")){continue;}
				if(count++ != 0){paramBuffer.append(", ");}
				paramBuffer.append( column + " = " + MARKER);
			}
		}
	}
	
	public Row getRow() {
		return row;
	}
	
	public void setRowProperties(List<Property> props) throws IllegalArgumentException{
		if(props == null || props.size() == 0){
			throw new IllegalArgumentException("In Properties can't be null or zero.");
		}
		row = new Row();
		for (Property property : props) {
			row.add(property);
		}
		super.setColumns(row.getKeys());
	}
	
	@Override
	protected void prepareTableName(String name) {
		pqlBuffer.append(getTableName() + " SET");
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
			
			if(whereBuffer.length() > 0){
				whereBuffer.append("WHERE ");
				int count = 0;
				for(Expression param : whereParams){
					if(param.getProperty().trim().equals("")){continue;}
					if(count++ != 0){whereBuffer.append( " " + getLogic().name() + " ");}
					whereBuffer.append( param.getProperty() + " " + param.getType().toString() + " " + MARKER);
				}
			}
		}
	}
	
	@Override
	protected void prepareWhereExpression(ExpressionInterpreter whereExpression) {
		whereBuffer.append("WHERE " + whereExpression.interpret());
	}
	
	public static String create(String tableName, String[]setParams, Logic whereLogic, String[] whereParams){
		return SQLUpdateQuery.create(tableName, setParams, whereLogic, Expression.createListFrom(whereParams, Operator.EQUAL));
	}
	
	public static String create(String tableName, String[]setParams, Logic whereLogic, List<Expression> whereParams){
		
		//Checking Illegal Arguments
		try{
			if(tableName == null || tableName.trim().equals("")){
				throw new IllegalArgumentException("Parameter 'tableName' must not be Null OR Empty.");
			}
			if(isAllParamEmpty(setParams)){
				throw new IllegalArgumentException("All Empty Parameters!!! You nuts (:D");
			}
		}catch(IllegalArgumentException iex){
			throw iex;
		}
		
		StringBuffer pqlBuffer = new StringBuffer("UPDATE " + tableName + " SET ");
		if(setParams != null && setParams.length > 0){
			int count = 0;
			for(String str : setParams){
				if(str.trim().equals("")){continue;}
				if(count++ != 0){pqlBuffer.append(", ");}
				pqlBuffer.append( str + " = " + MARKER);
			}
		}
		
		if(whereParams != null 
				&& whereParams.size() > 0
				&& !isAllParamEmpty(whereParams.toArray())){
			
			if(pqlBuffer.length() > 0){
				pqlBuffer.append(" WHERE ");
				int count = 0;
				for(Expression param : whereParams){
					if(param.getProperty().trim().equals("")){continue;}
					if(count++ != 0){pqlBuffer.append( " " + whereLogic.name() + " ");}
					pqlBuffer.append( param.getProperty() + param.getType().toString() + MARKER);
				}
			}
		}
		
		return pqlBuffer.toString();
	}
	
}