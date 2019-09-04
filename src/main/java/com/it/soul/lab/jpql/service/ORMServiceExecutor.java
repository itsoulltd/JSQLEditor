package com.it.soul.lab.jpql.service;

import com.it.soul.lab.jpql.query.JPQLQuery;
import com.it.soul.lab.jpql.query.JPQLSelectQuery;
import com.it.soul.lab.jpql.query.JPQLUpdateQuery;
import com.it.soul.lab.sql.QueryExecutor;
import com.it.soul.lab.sql.QueryTransaction;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.builder.AbstractQueryBuilder;
import com.it.soul.lab.sql.query.models.Expression;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ORMServiceExecutor<T> extends ORMService<T> implements QueryExecutor<JPQLSelectQuery, JPQLQuery,JPQLUpdateQuery, JPQLQuery, JPQLQuery>, QueryTransaction {

    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    public ORMServiceExecutor(EntityManager manager, String entity, Class<T> type) {
        super(manager, entity, type);
    }

    public ORMServiceExecutor(EntityManager manager, Class<T> type) {
        super(manager, type);
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
    public Integer executeInsert(boolean autoId, JPQLQuery insertQuery) throws SQLException, IllegalArgumentException {
        //TODO:
        return null;
    }

    @Override
    public Integer executeBatchDelete(int batchSize, JPQLQuery deleteQuery, List whereClause) throws SQLException {
        //TODO:
        return null;
    }

    @Override
    public Integer executeDelete(JPQLQuery deleteQuery) throws SQLException {
        //TODO:
        return null;
    }

    @Override
    public Integer[] executeBatchUpdate(int batchSize, JPQLUpdateQuery query, List updateProperties, List whereClause) throws SQLException, IllegalArgumentException {
        //TODO:
        return new Integer[0];
    }

    @Override
    public Integer executeUpdate(JPQLUpdateQuery query) throws SQLException {
        //TODO:
        return null;
    }

    @Override
    public Integer getScalerValue(JPQLQuery scalerQuery) throws SQLException {
        //TODO:
        return null;
    }

    @Override
    public List executeCRUDQuery(String query, Class type) throws SQLException, IllegalAccessException, InstantiationException {
        //TODO:
        return null;
    }

    @Override
    public Integer[] executeBatchInsert(boolean autoId, int batchSize, String tableName, List params) throws SQLException, IllegalArgumentException {
        //TODO:
        return new Integer[0];
    }

    @Override
    public List executeSelect(JPQLSelectQuery query, Class type, Map mappingKeys) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        TypedQuery<T> typedQuery = getEntityManager().createQuery(query.toString(), getEntityType());
        List<Expression> expressions = query.getWhereParamExpressions();
        if (expressions != null) {
            for (Expression item : expressions) {
                typedQuery.setParameter(item.getProperty(), item.getValueProperty().getValue());
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
