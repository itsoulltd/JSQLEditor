package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.*;
import com.it.soul.lab.sql.query.models.*;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AbstractQueryBuilder implements ColumnsBuilder, TableBuilder
        , WhereExpressionBuilder, InsertBuilder, ScalarExpressionBuilder, GroupByBuilder
        , HavingBuilder, JoinBuilder, JoinOnBuilder
        , IndexBuilder{

	private Logger LOG = Logger.getLogger(this.getClass().getSimpleName());
	private QueryType tempType = QueryType.SELECT;
	private SQLQuery tempQuery;

    protected void setTempType(QueryType type) {
        this.tempType = type;
    }

    protected void setTempQuery(SQLQuery sqlQuery) {
        this.tempQuery = sqlQuery;
    }

	@SuppressWarnings("unchecked")
	public <T extends SQLQuery> T build(){
		return (T) tempQuery;
	}

	protected SQLQuery factory(QueryType type){
		SQLQuery temp;
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
			temp = new SQLScalarQuery(ScalarType.COUNT);
			break;
		case MAX:
			temp = new SQLScalarQuery(ScalarType.MAX);
			break;
		case MIN:
			temp = new SQLScalarQuery(ScalarType.MIN);
			break;
		case AVG:
			temp = new SQLScalarQuery(ScalarType.AVG);
			break;
		case SUM:
			temp = new SQLScalarQuery(ScalarType.SUM);
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
        setTempType(type);
		setTempQuery(temp);
		return temp;
	}
	@Override
	public WhereExpressionBuilder from(String name){
		tempQuery.setTableName(name);
		return this;
	}
	@Override
	public ScalarExpressionBuilder on(String name) {
		tempQuery.setTableName(name);
		return this;
	}
	@Override
	public InsertBuilder into(String name) {
		tempQuery.setTableName(name);
		return this;
	}
    @Override
    public WhereExpressionBuilder rowsFrom(String name) {
        tempQuery.setTableName(name);
        return this;
    }
	@Override
	public TableBuilder columns(String... name){
		tempQuery.setColumns(name);
		return this;
	}
	@Override
	public GroupByBuilder where(Logic logic, String... name){
		if (logic != null){tempQuery.setLogic(logic);}
		tempQuery.setWhereParams(name);
		return this;
	}
	@Override
	public GroupByBuilder where(Logic logic, Expression... comps){
		if (logic != null){tempQuery.setLogic(logic);}
		List<Expression> items = Arrays.asList(comps);
		tempQuery.setWhereParamExpressions(items);
		return this;
	}
	@Override
	public QueryBuilder where(Property prop, Expression comps) {
		if(tempQuery instanceof SQLScalarQuery){
			((SQLScalarQuery)tempQuery).setScalerClouse(prop, comps);
		}
		return this;
	}
	@Override
	public QueryBuilder values(Property... properties) {
		if(tempQuery instanceof SQLInsertQuery){
			try{
				((SQLInsertQuery)tempQuery).setRowProperties(Arrays.asList(properties));
			}catch(IllegalArgumentException are){
				LOG.log(Level.WARNING, are.getMessage(), are);
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
				LOG.log(Level.WARNING,are.getMessage(), are);
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
		return orderBy(Operator.ASC, columns);
	}
	@Override
	public LimitBuilder orderBy(Operator order, String...columns){
    	List<String> colAsList = null;
    	if (order == Operator.DESC || order == Operator.ASC) {
			colAsList = Arrays.asList(columns)
					.stream()
					.map(col -> {
						if (col.toUpperCase().endsWith("ASC") || col.toUpperCase().endsWith("DESC"))
							return col;
						else
							return order.toString(col);
					})
					.collect(Collectors.toList());
		}
    	//
    	colAsList = (colAsList == null) ? Arrays.asList(columns) : colAsList;
		if (tempQuery instanceof SQLSelectQuery) {
			((SQLSelectQuery) tempQuery).setOrderBy(colAsList, null);
		} else if (tempQuery instanceof SQLJoinQuery) {
			((SQLJoinQuery) tempQuery).setOrderBy(colAsList, null);
		}
		return this;
	}
	@Override
	public HavingBuilder groupBy(String... columns) {
		if(tempQuery instanceof SQLSelectQuery) {
			((SQLSelectQuery)tempQuery).setGroupBy(Arrays.asList(columns));
		}else if(tempQuery instanceof SQLJoinQuery){
			((SQLJoinQuery) tempQuery).setGroupBy(Arrays.asList(columns));
		}
		return this;
	}
	@Override
	public OrderByBuilder having(ExpressionInterpreter expression) {
		if(tempQuery instanceof SQLSelectQuery) {
			((SQLSelectQuery)tempQuery).setHavingExpression(expression);
		}else if(tempQuery instanceof SQLJoinQuery){
			((SQLJoinQuery) tempQuery).setHavingExpression(expression);
		}
		return this;
	}
	@Override
	public JoinOnBuilder join(String table, String... columns) {
    	return joinAsAlice(table, null, columns);
	}
	@Override
	public JoinOnBuilder joinAsAlice(String table, String alice, String... columns) {
		if(tempQuery instanceof SQLJoinQuery) {
			((SQLJoinQuery)tempQuery).setJoins(table, alice, Arrays.asList(columns));
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
	public JoinBuilder on(String leftColumn, String rightColumn) {
		return on(new JoinExpression(leftColumn, rightColumn));
	}

	@Override
	public JoinOnBuilder rejoin(String table) {
		if(tempQuery instanceof SQLJoinQuery) {
			((SQLJoinQuery)tempQuery).setReJoins(table);
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

	@Override
	public JoinOnBuilder join(Class<? extends Entity> cType, String... columns) {
		return join(Entity.tableName(cType), columns);
	}

	@Override
	public JoinOnBuilder joinAsAlice(Class<? extends Entity> cType, String alice, String... columns) {
		return joinAsAlice(Entity.tableName(cType), alice, columns);
	}

	@Override
	public JoinOnBuilder rejoin(Class<? extends Entity> cType) {
		return rejoin(Entity.tableName(cType));
	}

	@Override
	public WhereExpressionBuilder from(Class<? extends Entity> cType) {
		return from(Entity.tableName(cType));
	}

	@Override
	public ScalarExpressionBuilder on(Class<? extends Entity> cType) {
		return on(Entity.tableName(cType));
	}

	@Override
	public InsertBuilder into(Class<? extends Entity> cType) {
		return into(Entity.tableName(cType));
	}

	@Override
	public WhereExpressionBuilder rowsFrom(Class<? extends Entity> cType) {
		return rowsFrom(Entity.tableName(cType));
	}
}
