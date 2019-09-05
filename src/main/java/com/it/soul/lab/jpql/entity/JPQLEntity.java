package com.it.soul.lab.jpql.entity;

import com.it.soul.lab.sql.QueryExecutor;
import com.it.soul.lab.sql.entity.Entity;

import java.sql.SQLException;

public abstract class JPQLEntity<T extends JPQLEntity> extends Entity {

    @Override
    public Boolean insert(QueryExecutor exe, String... keys) throws SQLException {
        boolean result = super.insert(exe, keys);
        return result;
    }

    @Override
    public Boolean update(QueryExecutor exe, String... keys) throws SQLException {
        boolean result = super.update(exe, keys);
        return result;
    }

    @Override
    public Boolean delete(QueryExecutor exe) throws SQLException {
        boolean result = super.delete(exe);
        return result;
    }

}
