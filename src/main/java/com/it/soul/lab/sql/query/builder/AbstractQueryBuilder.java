package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.*;
import com.it.soul.lab.sql.query.models.*;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractQueryBuilder implements ColumnsBuilder, TableBuilder
        , WhereClauseBuilder, InsertBuilder, ScalerClauseBuilder, GroupByBuilder
        , HavingBuilder, JoinBuilder, JoinOnBuilder
        , IndexBuilder{

	protected QueryType tempType = QueryType.SELECT;
	protected SQLQuery tempQuery;

	public AbstractQueryBuilder(){
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
	@Override
	public WhereClauseBuilder from(String name){
		tempQuery.setTableName(name);
		return this;
	}
	@Override
	public ScalerClauseBuilder on(String name) {
		tempQuery.setTableName(name);
		return this;
	}
	@Override
	public InsertBuilder into(String name) {
		tempQuery.setTableName(name);
		return this;
	}
    @Override
    public WhereClauseBuilder rowsFrom(String name) {
        tempQuery.setTableName(name);
        return this;
    }
	@Override
	public TableBuilder columns(String... name){
		tempQuery.setColumns(name);
		return this;
	}
	@Override
	public GroupByBuilder whereParams(Logic logic, String... name){
		if (logic != null){tempQuery.setLogic(logic);}
		tempQuery.setWhereParams(name);
		return this;
	}
	@Override
	public GroupByBuilder whereParams(Logic logic, Expression... comps){
		if (logic != null){tempQuery.setLogic(logic);}
		List<Expression> items = Arrays.asList(comps);
		tempQuery.setWhereParamExpressions(items);
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

    @Override
    public ColumnsBuilder index(String name) {
        return this;
    }

    @Override
    public ColumnsBuilder uniqueIndex(String name) {
        return this;
    }
}
