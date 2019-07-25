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
    Integer[] executeBatchUpdate(int batchSize, U query, List<Row> updateProperties, List<Row> whereClause) throws SQLException,IllegalArgumentException;

    Integer executeDelete(D deleteQuery) throws SQLException;
    Integer executeBatchDelete(int batchSize, D deleteQuery, List<Row> whereClause) throws SQLException;

    Integer executeInsert(boolean autoId, I insertQuery) throws SQLException, IllegalArgumentException;
    Integer[] executeBatchInsert(boolean autoId, int batchSize, String tableName, List<Row> params) throws SQLException,IllegalArgumentException;

    Integer getScalerValue(C scalerQuery) throws SQLException;

    <T> List<T> executeSelect(String query, Class<T> type, Map<String, String> mappingKeys) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException;
    <T> List<T> executeSelect(S query, Class<T> type, Map<String, String> mappingKeys) throws SQLException,IllegalArgumentException, IllegalAccessException, InstantiationException;

    <T extends Entity> List<T> executeCRUDQuery(String query, Class<T> type) throws SQLException, IllegalAccessException, InstantiationException;
}
