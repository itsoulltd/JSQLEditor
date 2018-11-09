package com.it.soul.lab.sql.query;

public class SQLDistinctQuery extends SQLSelectQuery {

	public SQLDistinctQuery() {
		this.pqlBuffer = new StringBuffer("SELECT DISTINCT ");
	}

}
