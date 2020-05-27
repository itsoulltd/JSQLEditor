package com.it.soul.lab.jpql.service;

import com.it.soul.lab.jpql.query.JPQLDeleteQuery;
import com.it.soul.lab.jpql.query.JPQLQuery;
import com.it.soul.lab.jpql.query.JPQLSelectQuery;
import com.it.soul.lab.jpql.query.JPQLUpdateQuery;
import com.it.soul.lab.sql.AbstractExecutor;
import com.it.soul.lab.sql.QueryExecutor;
import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLInsertQuery;
import com.it.soul.lab.sql.query.SQLQuery;
import com.it.soul.lab.sql.query.SQLScalarQuery;
import com.it.soul.lab.sql.query.builder.AbstractQueryBuilder;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.Row;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JPQLExecutor extends AbstractExecutor implements QueryExecutor<JPQLSelectQuery, SQLInsertQuery, JPQLUpdateQuery, JPQLDeleteQuery, SQLScalarQuery> {

    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    private EntityManager entityManager = null;
    private boolean skipTransaction = false;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public JPQLExecutor(EntityManager manager) {
        this.entityManager = manager;
    }

    public JPQLExecutor(EntityManager entityManager, boolean skipTransaction) {
        this(entityManager);
        this.skipTransaction = skipTransaction;
    }

    @Override
    public AbstractQueryBuilder createQueryBuilder(QueryType queryType) {
        return new JPQLQuery.Builder(queryType);
    }

    @Override
    public Object createBlob(String val) throws SQLException {
        //TODO:
        return null;
    }

    @Override
    public Boolean executeDDLQuery(String query) throws SQLException {
        //TODO:
        return null;
    }

    @Override
    public List executeCRUDQuery(String query, Class type) throws SQLException, IllegalAccessException, InstantiationException {
        //TODO:
        return null;
    }

    private Integer executeUpdate(Query typedQuery) throws SQLException {
        if (typedQuery == null) return 0;
        int result = 0;
        boolean isNotAlreadyActive = !isTransactionActive();
        try {
            if (isNotAlreadyActive) begin();
            result = typedQuery.executeUpdate();
            if (isNotAlreadyActive) end();
        } catch (Exception e) {
            if (isNotAlreadyActive) abort();
        }
        return result;
    }

    @Override
    public Integer executeUpdate(JPQLUpdateQuery updateQuery) throws SQLException {
        Query typedQuery = getEntityManager().createQuery(updateQuery.toString());
        List<Property> values = updateQuery.getRow().getProperties();
        if (values != null) {
            for (Property item : values) {
                typedQuery.setParameter(item.getKey(), item.getValue());
            }
        }
        List<Expression> expressions = updateQuery.getWhereParamExpressions();
        if (expressions != null) {
            for (Expression expression : expressions) {
                typedQuery.setParameter(expression.getProperty(), expression.getValueProperty().getValue());
            }
        }
        return executeUpdate(typedQuery);
    }

    @Override
    public Integer[] executeUpdate(int size, JPQLUpdateQuery query, List<Row> rows) throws SQLException, IllegalArgumentException {
        throw new SQLException("Not Implemented YET");
    }

    @Override
    public Integer[] executeUpdate(int size, List<JPQLUpdateQuery> queries) throws SQLException, IllegalArgumentException {
        throw new SQLException("Not Implemented YET");
    }

    @Override
    public Integer executeInsert(boolean autoId, SQLInsertQuery insertQuery) throws SQLException, IllegalArgumentException {
        Query query = null;
        if (insertQuery instanceof SQLInsertQuery) {
            query = getEntityManager().createNativeQuery(insertQuery.toString()/*, getEntityType()*/);
            List<Property> values = insertQuery.getRow().getProperties();
            if (values != null) {
                int counter = 1;
                for (Property item : values) {
                    query.setParameter(counter++, item.getValue());
                }
            }
        }
        return executeUpdate(query);
    }

    @Override
    public Integer[] executeInsert(boolean autoId, int size, SQLInsertQuery insertQuery, List<Row> rows) throws SQLException, IllegalArgumentException {
        throw new SQLException("Not Implemented YET");
    }

    @Override
    public Integer executeDelete(JPQLDeleteQuery deleteQuery) throws SQLException {
        Query typedQuery = getEntityManager().createQuery(deleteQuery.toString());
        List<Expression> expressions = deleteQuery.getWhereParamExpressions();
        if (expressions != null) {
            for (Expression expression : expressions) {
                typedQuery.setParameter(expression.getProperty(), expression.getValueProperty().getValue());
            }
        }
        return executeUpdate(typedQuery);
    }

    @Override
    public Integer executeDelete(int size, JPQLDeleteQuery deleteQuery, List<Row> where) throws SQLException {
        throw new SQLException("Not Implemented YET");
    }

    @Override
    public Integer getScalarValue(SQLScalarQuery scalarQuery) throws SQLException {
        int result = 0;
        //Checking entityManager
        if (getEntityManager() == null || !getEntityManager().isOpen()) {
            return result;
        }
        if (scalarQuery instanceof SQLScalarQuery) {
            try {
                String pql = scalarQuery.toString();
                Query query = getEntityManager().createNativeQuery(pql);
                Object val = query.getSingleResult();
                if (val instanceof BigInteger) result = ((BigInteger) val).intValue();
                else if (val instanceof Long) result = ((Long) val).intValue();
            } catch (Exception e) {
                throw new SQLException(e.getMessage());
            }
        }
        return result;
    }

    @Override
    public <T> List<T> executeSelect(String query, Class<T> type, Map<String, String> mappingKeys) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        //TypedQuery<T> typedQuery = getEntityManager().createQuery(query, type);
        Query typedQuery = getEntityManager().createNativeQuery(query, type);
        return typedQuery.getResultList();
    }

    @Override
    public <T> List<T> executeSelect(JPQLSelectQuery query, Class<T> type, Map<String, String> mappingKeys) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        TypedQuery<T> typedQuery = getEntityManager().createQuery(query.toString(), type);
        List<Expression> expressions = query.getWhereParamExpressions();
        if (expressions != null) {
            for (Expression expression : expressions) {
                if (expression.getValueProperty().getValue() != null)
                    typedQuery.setParameter(expression.getProperty(), expression.getValueProperty().getValue());
            }
        }
        return typedQuery.getResultList();
    }

    protected boolean isTransactionActive() {
        return (skipTransaction) ? true : getEntityManager().getTransaction().isActive();
    }

    @Override
    public void begin() throws SQLException {
        if (!skipTransaction) getEntityManager().getTransaction().begin();
    }

    @Override
    public void end() throws SQLException {
        if (!skipTransaction) getEntityManager().getTransaction().commit();
    }

    @Override
    public void abort() throws SQLException {
        if (!skipTransaction) getEntityManager().getTransaction().rollback();
    }

    @Override
    public void close() throws Exception {
        if (getEntityManager().isOpen()) {
            try {
                if (!isTransactionActive()) begin();
                getEntityManager().flush();
                end();
            } catch (SQLException e) {
                log.warning(e.getMessage());
                abort();
            } finally {
                getEntityManager().close();
            }
        }
    }

    public int rowCount(Class<? extends Entity> type) throws Exception {
        int result = 0;
        //Checking entityManager
        if (getEntityManager() == null || !getEntityManager().isOpen()) {
            return result;
        }
        try {
            //String pql = "SELECT COUNT(u) FROM "+ Entity.tableName(type) +" u";
            SQLScalarQuery scalar = new SQLQuery.Builder(QueryType.COUNT)
                    .columns()
                    .on(Entity.tableName(type)).build();
            result = getScalarValue(scalar);
        } catch (PersistenceException e) {
            throw e;
        }
        return result;
    }
}
