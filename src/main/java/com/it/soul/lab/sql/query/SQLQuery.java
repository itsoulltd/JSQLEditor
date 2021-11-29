package com.it.soul.lab.sql.query;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.it.soul.lab.connect.DriverClass;
import com.it.soul.lab.sql.query.builder.AbstractQueryBuilder;
import com.it.soul.lab.sql.query.models.*;

public abstract class SQLQuery {
	
	public static class Builder extends AbstractQueryBuilder {
		
		public Builder(QueryType type){
			factory(type);
		}
		
		@SuppressWarnings("unchecked")
		public <T extends SQLQuery> T build(){
			return (T) super.build();
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

	public static boolean isAllParamEmpty(Object[]paramList){
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
	protected static final char QUANTIFIER = 'e';
	protected static final char STARIC = '*';
	protected static final char MARKER = '?';
	
	protected String queryString(DriverClass dialect) throws IllegalArgumentException{
		if(tableName == null || tableName.trim().equals("")){
			throw new IllegalArgumentException("Parameter 'tableName' must not be Null OR Empty.");
		}
		return "";
	}
	
	@Override
	public String toString() {
		return queryString(DriverClass.MYSQL).trim();
	}
	public String bindValueToString(){return queryString(DriverClass.MYSQL).trim();}
	
	private String tableName;
	private String[] columns;
	private String[] whereParams;
	private Logic logic = Logic.AND;
	private List<Expression> whereParamExpressions;
	private ExpressionInterpreter whereExpression;

    protected StringBuffer bindValueToQueryBuffer(StringBuffer buffer, Row row){
        String[] keySet = row.getKeys();
        Map<String, Property> propertyMap = row.keyValueMap();
        //
        int index = 0; //Start Index;
        for (String key : keySet) { //So that, order get preserved
            Property entry = propertyMap.get(key);
            index = buffer.indexOf("?", index);
            if (!(index < 0)){ // index >= Math.min(fromIndex, length-of-string)
				if (entry.getValue() == null) {
					buffer.replace(index, index+1, "NULL");
					continue;
				}
                //Check for dataType:
                if (entry.getType() == DataType.STRING
                        || entry.getType() == DataType.OBJECT
						|| entry.getType() == DataType.SQLDATE
						|| entry.getType() == DataType.SQLTIMESTAMP)
                    buffer.replace(index, index+1,"'"+ entry.getValue().toString()+"'");
                else if (entry.getType() == DataType.LIST){
                    StringBuffer inner = new StringBuffer();
                    ((List)entry.getValue()).forEach(o -> {
                        if (o instanceof String){
                            inner.append(",'"+o.toString()+"'");
                        }else {
                            inner.append(","+o.toString());
                        }
                    });
                    buffer.replace(index, index+1, inner.toString().replaceFirst(",",""));
                }
                else
                    buffer.replace(index, index+1, entry.getValue().toString());
            }
        }
        return buffer;
    }

}
