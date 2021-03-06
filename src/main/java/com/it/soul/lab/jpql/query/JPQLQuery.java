package com.it.soul.lab.jpql.query;

import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLQuery;
import com.it.soul.lab.sql.query.builder.AbstractQueryBuilder;

public class JPQLQuery extends SQLQuery{

public static class Builder extends AbstractQueryBuilder {
		
		public Builder(QueryType type){
			factory(type);
		}
		
		@SuppressWarnings("unchecked")
		public <T extends SQLQuery> T build(){
			return (T) super.build();
		}
		
		protected SQLQuery factory(QueryType type){
			SQLQuery temp = null;
			switch (type) {
			case SELECT:
				temp = new JPQLSelectQuery();
				break;
			case UPDATE:
				temp = new JPQLUpdateQuery();
				break;
            case INSERT:
                temp = new JPQLInsertQuery();
                break;
            case DELETE:
                temp = new JPQLDeleteQuery();
                break;
			default:
				temp = super.factory(type);
				break;
			}
			setTempType(type);
			setTempQuery(temp);
			return temp;
		}
		
	}
	
}
