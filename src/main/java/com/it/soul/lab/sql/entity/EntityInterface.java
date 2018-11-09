package com.it.soul.lab.sql.entity;

import java.sql.SQLException;

import com.it.soul.lab.sql.SQLExecutor;

public interface EntityInterface {
	public Boolean update(SQLExecutor exe, String...keys) throws SQLException, Exception;
	public Boolean insert(SQLExecutor exe, String...keys) throws SQLException, Exception;
	public Boolean delete(SQLExecutor exe) throws SQLException, Exception;
}
