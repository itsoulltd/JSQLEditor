package com.it.soul.lab.sql.query.builder;

import java.util.Arrays;
import java.util.List;

import com.it.soul.lab.sql.query.SQLDeleteQuery;
import com.it.soul.lab.sql.query.SQLDistinctQuery;
import com.it.soul.lab.sql.query.SQLInsertQuery;
import com.it.soul.lab.sql.query.SQLJoinQuery;
import com.it.soul.lab.sql.query.SQLQuery;
import com.it.soul.lab.sql.query.models.Logic;
import com.it.soul.lab.sql.query.models.Operator;
import com.it.soul.lab.sql.query.SQLQuery.QueryType;
import com.it.soul.lab.sql.query.SQLScalerQuery;
import com.it.soul.lab.sql.query.SQLSelectQuery;
import com.it.soul.lab.sql.query.SQLUpdateQuery;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;
import com.it.soul.lab.sql.query.models.JoinExpression;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.ScalerType;

public class QueryBuilderImpl implements ColumnsBuilder, TableBuilder
, WhereClauseBuilder, InsertBuilder, ScalerClauseBuilder, GroupByBuilder, HavingBuilder, JoinBuilder, JoinOnBuilder{

	protected QueryType tempType = QueryType.SELECT;
	protected SQLQuery tempQuery;

	public QueryBuilderImpl(){
		tempQuery = factory(tempType);
	}

	@SuppressWarnings("unchecked")
	public <T extends SQLQuery> T build(){
		return (T) tempQuery;
	}

	protected SQLQuery factory(QueryType type){
		SQLQuery temp = null;
		switch (type) {
		case DISTINCT:
			temp = new SQLDistinctQuery();
			break;
		case DELETE:
			temp = new SQLDeleteQuery();
			break;
		case INSERT:
			temp = new SQLInsertQuery();
			break;
		case UPDATE:
			temp = new SQLUpdateQuery();
			break;
		case COUNT:
			temp = new SQLScalerQuery(ScalerType.COUNT);
			break;
		case MAX:
			temp = new SQLScalerQuery(ScalerType.MAX);
			break;
		case MIN:
			temp = new SQLScalerQuery(ScalerType.MIN);
			break;
		case AVG:
			temp = new SQLScalerQuery(ScalerType.AVG);
			break;
		case SUM:
			temp = new SQLScalerQuery(ScalerType.SUM);
			break;
		case INNER_JOIN:
		case LEFT_JOIN:
		case RIGHT_JOIN:
		case FULL_JOIN:
			temp = new SQLJoinQuery(type);
			break;
		default:
			temp = new SQLSelectQuery();
			break;
		}
		return temp;
	}

	public WhereClauseBuilder from(String name){
		tempQuery.setTableName(name);
		return this;
	}
	public TableBuilder columns(String... name){
		tempQuery.setColumns(name);
		return this;
	}
	public GroupByBuilder whereParams(Logic logic, String... name){
		if (logic != null){tempQuery.setLogic(logic);}
		tempQuery.setWhereParams(name);
		return this;
	}
	public GroupByBuilder whereParams(Logic logic, Expression... comps){
		if (logic != null){tempQuery.setLogic(logic);}
		List<Expression> items = Arrays.asList(comps);
		tempQuery.setWhereParamExpressions(items);
		return this;
	}
	@Override
	public ScalerClauseBuilder on(String name) {
		if(tempQuery instanceof SQLScalerQuery){
			((SQLScalerQuery)tempQuery).setTableName(name);
		}
		return this;
	}
	@Override
	public QueryBuilder scalerClause(Property prop, Expression comps) {
		if(tempQuery instanceof SQLScalerQuery){
			((SQLScalerQuery)tempQuery).setScalerClouse(prop, comps);
		}
		return this;
	}
	@Override
	public InsertBuilder into(String name) {
		if(tempQuery instanceof SQLInsertQuery){
			((SQLInsertQuery)tempQuery).setTableName(name);
		}
		return this;
	}
	@Override
	public QueryBuilder values(Property... properties) {
		if(tempQuery instanceof SQLInsertQuery){
			try{
				((SQLInsertQuery)tempQuery).setRowProperties(Arrays.asList(properties));
			}catch(IllegalArgumentException are){
				System.out.println(are.getMessage());
			}
		}
		return this;
	}
	@Override
	public WhereClauseBuilder rowsFrom(String name) {
		if(tempQuery instanceof SQLDeleteQuery){
			((SQLDeleteQuery)tempQuery).setTableName(name);
		}
		return this;
	}
	@Override
	public TableBuilder set(Property... properties) {
		if (tempQuery instanceof SQLUpdateQuery){
			try{
				((SQLUpdateQuery)tempQuery).setRowProperties(Arrays.asList(properties));
			}catch(IllegalArgumentException are){
				System.out.println(are.getMessage());
			}
		}
		return this;
	}
	@Override
	public GroupByBuilder where(ExpressionInterpreter expression) {
		tempQuery.setWhereExpression(expression); 
		return this;
	}
	@Override
	public QueryBuilder addLimit(Integer limit, Integer offset) {
		if(tempQuery instanceof SQLSelectQuery) {
			((SQLSelectQuery)tempQuery).setLimit(limit, offset);
		}else if(tempQuery instanceof SQLJoinQuery) {
			((SQLJoinQuery)tempQuery).setLimit(limit, offset);
		}
		return this;
	}
	@Override
	public LimitBuilder orderBy(String... columns) {
		if(tempQuery instanceof SQLSelectQuery) {
			((SQLSelectQuery)tempQuery).setOrderBy(Arrays.asList(columns), Operator.ASC);
		}else if(tempQuery instanceof SQLJoinQuery) {
			((SQLJoinQuery)tempQuery).setOrderBy(Arrays.asList(columns), Operator.ASC);
		}
		return this;
	}
	@Override
	public HavingBuilder groupBy(String... columns) {
		if(tempQuery instanceof SQLSelectQuery) {
			((SQLSelectQuery)tempQuery).setGroupBy(Arrays.asList(columns));
		}
		return this;
	}
	@Override
	public OrderByBuilder having(ExpressionInterpreter expression) {
		if(tempQuery instanceof SQLSelectQuery) {
			((SQLSelectQuery)tempQuery).setHavingExpression(expression);
		}
		return this;
	}
	@Override
	public JoinOnBuilder join(String table, String... columns) {
		if(tempQuery instanceof SQLJoinQuery) {
			((SQLJoinQuery)tempQuery).setJoins(table, Arrays.asList(columns));
		}
		return this;
	}
	@Override
	public JoinBuilder on(JoinExpression expression) {
		if(tempQuery instanceof SQLJoinQuery) {
			((SQLJoinQuery)tempQuery).setJoinExpression(expression);
		}
		return this;
	}
}
