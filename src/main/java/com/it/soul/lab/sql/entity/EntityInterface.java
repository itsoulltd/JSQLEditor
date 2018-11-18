package com.it.soul.lab.sql.entity;

import java.sql.SQLException;

import com.it.soul.lab.sql.QueryExecutor;

public interface EntityInterface {
	public Boolean update(QueryExecutor exe, String...keys) throws SQLException, Exception;
	public Boolean insert(QueryExecutor exe, String...keys) throws SQLException, Exception;
	public Boolean delete(QueryExecutor exe) throws SQLException, Exception;
}
