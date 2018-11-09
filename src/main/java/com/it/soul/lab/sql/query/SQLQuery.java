package com.it.soul.lab.sql.query;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.it.soul.lab.sql.query.builder.QueryBuilderImpl;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;
import com.it.soul.lab.sql.query.models.Logic;
import com.it.soul.lab.sql.query.models.Operator;
import com.it.soul.lab.sql.query.models.Row;

public class SQLQuery {
	
	public static class Builder extends QueryBuilderImpl{
		
		public Builder(QueryType type){
			tempType = type;
			tempQuery = factory(tempType);
		}
		
		@SuppressWarnings("unchecked")
		public <T extends SQLQuery> T build(){
			return (T) tempQuery;
		}
		
	}
	
	//////////////////////////////////SQLQuery///////////////////////////////////////////
	
	public ExpressionInterpreter getWhereExpression() {
		return whereExpression;
	}

	public void setWhereExpression(ExpressionInterpreter whereExpression) {
		this.whereExpression = whereExpression;
		Expression[] comps = whereExpression.resolveExpressions();
		this.whereParamExpressions = Arrays.asList(comps);
	}
	
	public List<Expression> getWhereParamExpressions() {
		return whereParamExpressions;
	}
	
	public void setWhereParamExpressions(List<Expression> params) {
		this.whereParamExpressions = params;
	}
	
	public Row getWhereProperties() {
		return Expression.convertToRow(whereParamExpressions);
	}

	protected static boolean isAllParamEmpty(Object[]paramList){
		boolean result = false;
		if(paramList != null && paramList.length > 0){
			int count = 0;
			for(Object item : paramList){
				
				if(item.toString().trim().equals(""))
					continue;
				count++;
			}
			result = (count == 0) ? true : false;
		}
		return result;
	}
	
	public String[] getWhereParams() {
		if (whereParams == null && whereParamExpressions != null) {
			return getWhereProperties().getKeys();
		}
		return whereParams;
	}
	public void setWhereParams(String[] whereParams) {
		this.whereParams = whereParams;
		if(whereParamExpressions == null){
			whereParamExpressions = new ArrayList<Expression>();
			for (String params : whereParams) {
				whereParamExpressions.add(new Expression(params, Operator.EQUAL));
			}
		}
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String[] getColumns() {
		return columns;
	}
	public void setColumns(String[] columns) {
		this.columns = columns;
	}
	public Logic getLogic() {
		return logic;
	}
	public void setLogic(Logic logic) {
		this.logic = logic;
	}
	protected static final char QUIENTIFIER = 'e';
	protected static final char STARIC = '*';
	protected static final char MARKER = '?';
	
	protected String queryString() throws IllegalArgumentException{
		if(tableName == null || tableName.trim().equals("")){
			throw new IllegalArgumentException("Parameter 'tableName' must not be Null OR Empty.");
		}
		return "";
	}
	
	@Override
	public String toString() {
		return queryString().trim();
	}
	
	private String tableName;
	private String[] columns;
	private String[] whereParams;
	private Logic logic = Logic.AND;
	private List<Expression> whereParamExpressions;
	private ExpressionInterpreter whereExpression;
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////SQLQuery-Enums//////////////////////////////////////////////
	
	public enum QueryType{
		SELECT,
		COUNT,
		DISTINCT,
		INSERT,
		UPDATE,
		DELETE,
		MAX,
		MIN,
		AVG,
		SUM,
		INNER_JOIN,
		LEFT_JOIN,
		RIGHT_JOIN,
		FULL_JOIN
	}

}
