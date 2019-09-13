package com.it.soul.lab.sql.entity;

import com.it.soul.lab.sql.QueryExecutor;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Map;

public interface EntityInterface {
	Boolean update(QueryExecutor exe, String...keys) throws SQLException, Exception;
	Boolean insert(QueryExecutor exe, String...keys) throws SQLException, Exception;
	Boolean delete(QueryExecutor exe) throws SQLException, Exception;
	Field getDeclaredField(String fieldName, boolean inherit) throws NoSuchFieldException;
    Field[] getDeclaredFields(boolean inherit);
	Map<String, Object> marshallingToMap(boolean inherit);
    void unmarshallingFromMap(Map<String, Object> data, boolean inherit);
}
