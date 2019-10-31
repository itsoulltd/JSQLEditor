package com.it.soul.lab.jpql.service;

import com.it.soul.lab.sql.entity.TableName;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Table;

public abstract class AbstractService<T> {

	private static final String _TAG = "Service";
	@SuppressWarnings("unused")
	private static final String _MESSAGE = "Service not available now!";
	
	private EntityManager entityManager = null;
	private String entity = null;
	private Class<T> entityType = null;
	
	private AbstractService(){}
	
	public AbstractService(EntityManager manager, Class<T> type){
		this();
		this.entityManager = manager;
		this.entity = type.getSimpleName();
		if (type.isAnnotationPresent(Entity.class)){
		    String name = type.getAnnotation(Entity.class).name();
		    entity = (name != null || !name.isEmpty()) ? name : entity;
        }else if (type.isAnnotationPresent(Table.class)){
            String name = type.getAnnotation(Table.class).name();
            entity = (name != null || !name.isEmpty()) ? name : entity;
        }else if (type.isAnnotationPresent(TableName.class)){
            String name = type.getAnnotation(TableName.class).value();
            entity = (name != null || !name.isEmpty()) ? name : entity;
        }
		this.entityType = type;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public String getEntity() {
		return entity;
	}

	public Class<T> getEntityType() {
		return entityType;
	}
	
	@Override
	public String toString() {
		return _TAG;
	}
	
}
