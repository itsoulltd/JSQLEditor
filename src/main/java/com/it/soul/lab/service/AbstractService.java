package com.it.soul.lab.service;

import javax.persistence.EntityManager;

public abstract class AbstractService<T> {

	private static final String _TAG = "Service";
	@SuppressWarnings("unused")
	private static final String _MESSAGE = "Service not available now!";
	
	private EntityManager entityManager = null;
	private String entity = null;
	private Class<T> entityType = null;
	
	private AbstractService(){}
	
	public AbstractService(EntityManager manager, String entity, Class<T> type){
		this();
		this.entityManager = manager;
		this.entity = entity;
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
