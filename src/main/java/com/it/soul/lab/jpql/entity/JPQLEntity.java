package com.it.soul.lab.jpql.entity;

import com.it.soul.lab.jpql.service.ORMServiceProtocol;
import com.it.soul.lab.sql.QueryExecutor;
import com.it.soul.lab.sql.entity.Entity;

import java.sql.SQLException;
import java.util.logging.Logger;

public abstract class JPQLEntity<T extends JPQLEntity> extends Entity {

    protected Logger log = Logger.getLogger(this.getClass().getSimpleName());

    @Override
    public Boolean insert(QueryExecutor exe, String... keys) throws SQLException {
        boolean result = false;
        if (ORMServiceProtocol.class.isAssignableFrom(exe.getClass())){
            ORMServiceProtocol<T> protocol = (ORMServiceProtocol<T>) exe;
            try {
                Entity ent = protocol.insert((T) this);
                result = (ent != null);
            } catch (Exception e) {
                log.warning(e.getMessage());
                throw new SQLException(e.getMessage());
            }
        }
        return result;
    }

    @Override
    public Boolean update(QueryExecutor exe, String... keys) throws SQLException {
        boolean result = false;
        if (ORMServiceProtocol.class.isAssignableFrom(exe.getClass())){
            ORMServiceProtocol<T> protocol = (ORMServiceProtocol<T>) exe;
            try {
                Entity ent = protocol.update((T) this);
                result = (ent != null);
            } catch (Exception e) {
                log.warning(e.getMessage());
                throw new SQLException(e.getMessage());
            }
        }
        return result;
    }

    @Override
    public Boolean delete(QueryExecutor exe) throws SQLException {
        boolean result = false;
        if (ORMServiceProtocol.class.isAssignableFrom(exe.getClass())){
            ORMServiceProtocol<T> protocol = (ORMServiceProtocol<T>) exe;
            try {
                result = protocol.delete((T) this);
            } catch (Exception e) {
                log.warning(e.getMessage());
                throw new SQLException(e.getMessage());
            }
        }
        return result;
    }

}
