package com.it.soul.lab.sql;

import com.it.soul.lab.sql.query.SQLInsertQuery;
import com.it.soul.lab.sql.query.SQLQuery;
import com.it.soul.lab.sql.query.SQLScalerQuery;
import com.it.soul.lab.sql.query.models.Row;

import java.sql.SQLException;
import java.util.List;

public interface QueryExecutor<S extends SQLQuery
        , I extends SQLQuery
        , U extends SQLQuery
        , D extends SQLQuery
        , C extends SQLQuery> extends AutoCloseable {

    Boolean executeDDLQuery(String query) throws SQLException;

    Integer executeUpdate(U query) throws SQLException;
    Integer[] executeBatchUpdate(int batchSize, U query, List<Row> updateProperties, List<Row> whereClause) throws SQLException,IllegalArgumentException;

    Integer executeDelete(D deleteQuery) throws SQLException;
    Integer executeBatchDelete(int batchSize, D deleteQuery, List<Row> whereClause) throws SQLException;

    Integer executeInsert(boolean isAutoGenaretedId, String query) throws SQLException, IllegalArgumentException;
    Integer executeInsert(boolean isAutoGenaretedId, I insertQuery) throws SQLException, IllegalArgumentException;
    Integer[] executeBatchInsert(boolean isAutoGenaretedId, int batchSize, String tableName, List<Row> params) throws SQLException,IllegalArgumentException;

    Integer getScalerValue(String query) throws SQLException;
    Integer getScalerValue(C scalerQuery) throws SQLException;

    <T> List<T> executeCRUDQuery(String query, Class<T> type) throws SQLException, IllegalAccessException, InstantiationException;
    <T> List<T> executeSelect(String query, Class<T> type) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException;
    <T> List<T> executeSelect(S query, Class<T> type) throws SQLException,IllegalArgumentException, IllegalAccessException, InstantiationException;
}
