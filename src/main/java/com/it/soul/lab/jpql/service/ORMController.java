package com.it.soul.lab.jpql.service;

import java.io.Serializable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.Persistence;


public class ORMController implements Serializable{

	private static final long serialVersionUID = -873194077323932977L;
	private EntityManagerFactory emf = null;
	private EntityManager em = null;
	
	public ORMController(String persistenceUnitName) throws IllegalStateException{
		createEntityManager(persistenceUnitName);
	}
	
	public EntityManager getEntityManager() {
		return em;
	}
	
	public void clearEntityManager() throws Exception{
		if(em != null){
			try{
				em.getTransaction().begin();
				em.flush();
				em.getTransaction().commit();
			}catch(Exception e){
				throw e;
			}finally{
				em.clear();
			}
		}
	}

	private void createEntityManager(String persistenceUnit) throws IllegalStateException{
		if(em == null){
			emf = Persistence.createEntityManagerFactory(persistenceUnit); 
			em = emf.createEntityManager();
			//Avoids find causing flush.
			em.setFlushMode(FlushModeType.COMMIT);
		}
	}
	
	public void closeEntityManager() throws IllegalStateException,Exception{
		if(em != null){
			try{
				if(em.isOpen()){
					em.getTransaction().begin();
					em.flush();
					em.getTransaction().commit();
				}
			}catch(IllegalStateException e){
				em.getTransaction().rollback();
				throw e;
			}catch(Exception e){
				em.getTransaction().rollback();
				throw e;
			}finally{
				em.clear();
				em.close();
				em = null;
				if (emf.isOpen())
					emf.close();
			}
		}
	}
	
}
