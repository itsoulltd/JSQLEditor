package com.it.soul.lab.service;

import java.util.Collection;
import java.util.Map;

import javax.persistence.EntityManager;
import com.it.soul.lab.sql.query.models.Logic;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;
import com.it.soul.lab.sql.query.models.Property;


public interface ORMServiceProtocol<T> {

	public Class<T> getEntityType();
	public String getEntity();
	public EntityManager getEntityManager();
	
	public Collection<T> read() throws Exception;
	public Collection<T> read(String...columns) throws Exception;
	public Collection<T> read(ExpressionInterpreter expression , String...columns) throws Exception;
	@Deprecated public Collection<T> findAll(Property item, String...columns) throws Exception;
	@Deprecated public Collection<T> findAll(Map<String,Object> itemIds, Logic whereLogic, String...columns) throws Exception;
	
	public T readBy(Property searchProperty) throws Exception;
	public T readBy(Property searchProperty, String...columns) throws Exception;
	
	public boolean exist(Object itemId) throws Exception;
	public long rowCount() throws Exception;
	public T insert(T item) throws Exception;
	public T update(T item) throws Exception;
	public boolean delete(T item) throws Exception;
	public Collection<T> batchInsert(Collection<T> items) throws Exception;
	public Collection<T> batchUpdate(Collection<T> items) throws Exception;
	public boolean batchDelete(Collection<T> items) throws Exception;
	public Collection<T> batchInsert(Collection<T> items, int batchSize) throws Exception;
	public Collection<T> batchUpdate(Collection<T> items, int batchSize) throws Exception;
	public T refresh(T item) throws Exception;
	public Collection<T> refresh(Collection<T> items) throws Exception;
	public void clearItem(T item) throws Exception;
	public void clearItems(Collection<T> items) throws Exception;
}
