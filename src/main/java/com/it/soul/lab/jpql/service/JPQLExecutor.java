package com.it.soul.lab.jpql.service;

import com.it.soul.lab.jpql.query.JPQLDeleteQuery;
import com.it.soul.lab.jpql.query.JPQLQuery;
import com.it.soul.lab.jpql.query.JPQLSelectQuery;
import com.it.soul.lab.jpql.query.JPQLUpdateQuery;
import com.it.soul.lab.sql.QueryExecutor;
import com.it.soul.lab.sql.QueryTransaction;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLInsertQuery;
import com.it.soul.lab.sql.query.SQLScalerQuery;
import com.it.soul.lab.sql.query.builder.AbstractQueryBuilder;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.Row;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ORMServiceExecutor implements QueryExecutor<JPQLSelectQuery, SQLInsertQuery,JPQLUpdateQuery, JPQLDeleteQuery, SQLScalerQuery>, QueryTransaction {

    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    private EntityManager entityManager = null;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public ORMServiceExecutor(EntityManager manager) {
        this.entityManager = manager;
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
        try{
            begin();
            result = typedQuery.executeUpdate();
            end();
        }catch (Exception e){
            abort();
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
    public Integer[] executeBatchUpdate(int batchSize, JPQLUpdateQuery query, List updateProperties, List whereClause) throws SQLException, IllegalArgumentException {
        //TODO:
        return new Integer[0];
    }

    @Override
    public Integer executeInsert(boolean autoId, SQLInsertQuery insertQuery) throws SQLException, IllegalArgumentException {
        Query query = null;
        if (insertQuery instanceof SQLInsertQuery){
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
    public Integer[] executeBatchInsert(boolean autoId, int batchSize, String tableName, List params) throws SQLException, IllegalArgumentException {
        //TODO:
        return new Integer[0];
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
    public Integer executeBatchDelete(int batchSize, JPQLDeleteQuery deleteQuery, List<Row> whereClause) throws SQLException {
        return null;
    }

    @Override
    public Integer getScalerValue(SQLScalerQuery scalerQuery) throws SQLException {
        return null;
    }

    @Override
    public <T> List<T> executeSelect(JPQLSelectQuery query, Class<T> type, Map mappingKeys) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
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

    @Override
    public List executeSelect(String query, Class type, Map mappingKeys) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        TypedQuery<T> typedQuery = getEntityManager().createQuery(query, getEntityType());
        return typedQuery.getResultList();
    }

    @Override
    public void begin() throws SQLException {
        getEntityManager().getTransaction().begin();
    }

    @Override
    public void end() throws SQLException {
        getEntityManager().getTransaction().commit();
    }

    @Override
    public void abort() throws SQLException {
        getEntityManager().getTransaction().rollback();
    }

    @Override
    public void close() throws Exception {
        if (getEntityManager().isOpen()){
            try {
                begin();
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
}
