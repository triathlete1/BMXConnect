package com.biomerieux.bmxconnect.server;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class DAO<T> {

	@SuppressWarnings("unchecked")
	public List<T> queryList(String queryStr, Object... parameterMap) {
		if (parameterMap.length % 2 != 0) {
			throw new IllegalArgumentException("queryList parameterMap must contain {key:value} pairs! An odd number of arguments was detected: " + parameterMap);
		}

		List<T> results = new ArrayList<T>();

		EntityManager em = EMFService.get().createEntityManager();
	    try {
	    	Query query = em.createQuery(queryStr);
	    	String key = null;
	    	Object value;
	    	int index = 0;
	    	for (Object param : parameterMap) {
	    		if (index++ % 2 == 0) {
	    			key = (String)param;
	    			continue;
	    		}
	    		value = param;
	    		query.setParameter(key, value);
	    	}
	    	results.addAll(query.getResultList());		
	    } finally {
	    	em.close();
	    }
	    return results;
	}
	
	public void delete(T record) {
	    EntityManager em = EMFService.get().createEntityManager();
	    try {
			em.remove(record);
	    } finally {
	    	em.close();
	    }
	}
	
	public void save(T record) {
	    EntityManager em = EMFService.get().createEntityManager();
	    try {
			em.persist(record);
	    } finally {
	    	em.close();
	    }
	}
}
