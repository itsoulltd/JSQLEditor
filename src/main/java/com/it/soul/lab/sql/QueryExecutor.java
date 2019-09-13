package com.it.soul.lab.sql;

import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLQuery;
import com.it.soul.lab.sql.query.builder.AbstractQueryBuilder;
import com.it.soul.lab.sql.query.models.Row;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface QueryExecutor<S extends SQLQuery
        , I extends SQLQuery
        , U extends SQLQuery
        , D extends SQLQuery
        , C extends SQLQuery> extends AutoCloseable {

    AbstractQueryBuilder createQueryBuilder(QueryType queryType);
    Object createBlob(String val) throws SQLException;
    Boolean executeDDLQuery(String query) throws SQLException;

    Integer executeUpdate(U query) throws SQLException;
    Integer[] executeUpdate(int size, U query, List<Row> rows) throws SQLException,IllegalArgumentException;
    Integer[] executeUpdate(int size, List<U> queries) throws SQLException,IllegalArgumentException;

    Integer executeDelete(D deleteQuery) throws SQLException;
    Integer executeDelete(int size, D deleteQuery, List<Row> where) throws SQLException;

    Integer executeInsert(boolean autoId, I insertQuery) throws SQLException, IllegalArgumentException;
    Integer[] executeInsert(boolean autoId, int size, I insertQuery, List<Row> rows) throws SQLException,IllegalArgumentException;

    Integer getScalarValue(C scalarQuery) throws SQLException;

    <T> List<T> executeSelect(String query, Class<T> type, Map<String, String> mappingKeys) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException;
    <T> List<T> executeSelect(S query, Class<T> type, Map<String, String> mappingKeys) throws SQLException,IllegalArgumentException, IllegalAccessException, InstantiationException;

    <T extends Entity> List<T> executeCRUDQuery(String query, Class<T> type) throws SQLException, IllegalAccessException, InstantiationException;
}
