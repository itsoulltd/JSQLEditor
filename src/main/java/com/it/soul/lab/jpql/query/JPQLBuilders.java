package com.it.soul.lab.jpql.query;

import java.util.Map;
import java.util.Map.Entry;

import com.it.soul.lab.sql.query.SQLQuery;
import com.it.soul.lab.sql.query.models.Operator;
import com.it.soul.lab.sql.query.models.Logic;

@Deprecated @SuppressWarnings("Duplicates")
public class JPQLBuilders {
	
	private static final char QUANTIFIER = 'e';
	
	/**
	 * 
	 * @param tableName
	 * @param projectionParams
	 * @return
	 */
	@Deprecated
	public static String createSelectQuery(String tableName, String[]projectionParams)
	throws IllegalArgumentException{
		
		//Checking Illegal Arguments
		try{
			if(tableName == null || tableName.trim().equals("")){
				throw new IllegalArgumentException("Parameter 'tableName' must not be Null OR Empty.");
			}
		}catch(IllegalArgumentException iex){
			throw iex;
		}
		
		//Query Builders
		StringBuffer pqlBuffer = new StringBuffer("SELECT ");
		if(projectionParams != null && projectionParams.length > 0){
			
			int count = 0;
			for(String str : projectionParams){
				
				if(str.trim().equals("")){
					continue;
				}
				if(count++ != 0){
					pqlBuffer.append(", ");
				}
				pqlBuffer.append( QUANTIFIER + "." + str);
			}
			//If all passed parameter is empty
			if(count == 0){
				pqlBuffer.append(QUANTIFIER);
			}
		}else{
			
			pqlBuffer.append(QUANTIFIER);
		}
		
		pqlBuffer.append(" FROM "+ tableName + " " + QUANTIFIER);
		//
		return pqlBuffer.toString();
	}
	
	/**
	 * 
	 * @param tableName
	 * @param projectionParams
	 * @param whereLogic
	 * @param whereParams
	 * @return
	 */
	@Deprecated
	public static String createSelectQuery(String tableName, String[]projectionParams, Logic whereLogic, String[] whereParams)
	throws IllegalArgumentException{
		
		//Query Builders
		StringBuffer pqlBuffer = null;
		try{
			pqlBuffer = new StringBuffer(createSelectQuery(tableName, projectionParams));
			if(isAllParamEmpty(whereParams)){
				throw new IllegalArgumentException("All Empty Parameters!!! You nuts (:D");
			}
		}catch(IllegalArgumentException iex){
			throw iex;
		}
		
		if(pqlBuffer.length() > 0){
			pqlBuffer.append(" WHERE ");
			
			int count = 0;
			for(String param : whereParams){
				
				if(param.trim().equals("")){
					continue;
				}
				if(count++ != 0){
					pqlBuffer.append( " " + whereLogic.name() + " ");
				}
				pqlBuffer.append( QUANTIFIER + "." + param + " = :" + param);
			}
		}
		//
		return pqlBuffer.toString();
	}
	
	/**
	 * 
	 * @param tableName
	 * @param projectionParams
	 * @param whereLogic
	 * @param whereParams must be a HashMap<String,String> where key="parameter" and value="Comparison operator e.g. '<=', '!=', '<', '=' "
	 * @return
	 */
	@Deprecated
	public static String createSelectQuery(String tableName, String[]projectionParams, Logic whereLogic, Map<String,Operator> whereParams)
	throws IllegalArgumentException{
		
		//Query Builders
		StringBuffer pqlBuffer = null;
		try{
			pqlBuffer = new StringBuffer(createSelectQuery(tableName, projectionParams));
			if(isAllParamEmpty(whereParams.keySet().toArray())
					|| isAllParamEmpty(whereParams.values().toArray())){
				throw new IllegalArgumentException("All Empty Parameters!!! You nuts (:D");
			}
		}catch(IllegalArgumentException iex){
			throw iex;
		}
		
		if(pqlBuffer.length() > 0){
			pqlBuffer.append(" WHERE ");
			
			int count = 0;
			for( Entry<String, Operator> param : whereParams.entrySet()){
				
				if(param.getKey().trim().equals("")){
					continue;
				}
				if(count++ != 0){
					pqlBuffer.append( " " + whereLogic.name() + " ");
				}
				pqlBuffer.append( QUANTIFIER + "." + param.getKey()+ " " + param.getValue().toString() + " :" + param.getKey());
			}
		}
		//
		return pqlBuffer.toString();
	}
	
	/**
	 * 
	 * @param tableName
	 * @param setParams
	 * @param whereLogic
	 * @param whereParams
	 * @return
	 */
	@Deprecated
	public static String createUpdateQuery(String tableName, String[]setParams, Logic whereLogic, String[] whereParams){
		
		//Checking Illegal Arguments
		try{
			if(tableName == null || tableName.trim().equals("")){
				throw new IllegalArgumentException("Parameter 'tableName' must not be Null OR Empty.");
			}
			if(isAllParamEmpty(whereParams)){
				throw new IllegalArgumentException("All Empty Parameters!!! You nuts (:D");
			}
			if(isAllParamEmpty(setParams)){
				throw new IllegalArgumentException("All Empty Parameters!!! You nuts (:D");
			}
		}catch(IllegalArgumentException iex){
			throw iex;
		}
		
		StringBuffer pqlBuffer = new StringBuffer("UPDATE " + tableName + " " + QUANTIFIER + " SET ");
		
		if(setParams != null && setParams.length > 0){
			
			int count = 0;
			for(String str : setParams){
				
				if(str.trim().equals("")){
					continue;
				}
				if(count++ != 0){
					pqlBuffer.append(", ");
				}
				pqlBuffer.append( QUANTIFIER + "." + str + " = :" + str);
			}
		}
		
		if(pqlBuffer.length() > 0){
			pqlBuffer.append(" WHERE ");
			
			int count = 0;
			for(String param : whereParams){
				
				if(param.trim().equals("")){
					continue;
				}
				if(count++ != 0){
					pqlBuffer.append( " " + whereLogic.name() + " ");
				}
				pqlBuffer.append( QUANTIFIER + "." + param + " = :" + param);
			}
		}
		
		return pqlBuffer.toString();
	}
	
	private static boolean isAllParamEmpty(String[]paramList){
		
		boolean result = false;
		if(paramList != null && paramList.length > 0){
			
			int count = 0;
			for(String item : paramList){
				
				if(item.trim().equals(""))
					continue;
				count++;
			}
			result = (count == 0) ? true : false;
		}
		return result;
	}
	
	private static boolean isAllParamEmpty(Object[]paramList){
		return SQLQuery.isAllParamEmpty(paramList);
	}
	
}
