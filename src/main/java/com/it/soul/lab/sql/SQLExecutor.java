package com.it.soul.lab.sql;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.it.soul.lab.sql.query.SQLDeleteQuery;
import com.it.soul.lab.sql.query.SQLInsertQuery;
import com.it.soul.lab.sql.query.SQLQuery;
import com.it.soul.lab.sql.query.SQLQuery.QueryType;
import com.it.soul.lab.sql.query.SQLScalerQuery;
import com.it.soul.lab.sql.query.SQLSelectQuery;
import com.it.soul.lab.sql.query.SQLUpdateQuery;
import com.it.soul.lab.sql.query.models.DataType;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.Row;
import com.it.soul.lab.sql.query.models.Table;

public class SQLExecutor implements Serializable{

	private static final long serialVersionUID = 6052074650432885583L;
	private Connection conn = null;

	public SQLExecutor(Connection conn){ this.conn = conn; }
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();//so that unreleased statement object goes to garbage.
	}
	
	/**
     * Following object container holds all Statement object belongs to any connections
     */
    private List<Statement> statementHolder = null;
    
    private List<Statement> getStatementHolder() {
    	if(null == statementHolder){
    		statementHolder = new ArrayList<Statement>();
    	}
		return statementHolder;
	}
	
	public void close(){
		try {
			int count = getStatementHolder().size();
			Boolean isAllCloed = true;
			if(count > 0){
				for (Statement iterable_element : getStatementHolder()) {
					try{
						iterable_element.close();
					}catch (SQLException e){
						isAllCloed = false;
					}
				}
			}
			getStatementHolder().clear();
			System.out.println("Retain Statement count was "+ count + ". All has been Closed : "+ (isAllCloed ? "YES":"NO"));
			closeConnections(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void closeConnections(Connection conn) 
			throws SQLException{
		if(conn != null && !conn.isClosed()){
			try{
				if(!conn.getAutoCommit())
					conn.commit();
			}catch(SQLException exp){
				if(!conn.getAutoCommit())
					conn.rollback();
				throw exp;
			}
			finally{
				try {
					if(conn != null && !conn.isClosed())
						conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				System.out.println("Executor Has Closed.");
			}
		}
	}
	
	public void begin() throws SQLException{
		if(conn != null && conn.isClosed() == false) {
			conn.setAutoCommit(false);
		}
	}
	
	public void end() throws SQLException{
		if(conn != null && conn.isClosed() == false) {
			conn.commit();
			conn.setAutoCommit(true);
		}
	}
	
	public void abort() throws SQLException{
		if(conn != null && conn.isClosed() == false) {
			conn.rollback();
			conn.setAutoCommit(true);
		}
	}
	
	/**
	 * Display rows in a Result Set
	 * @param rst
	 */
	public void displayResultSet(ResultSet rst){
		StringBuffer buffer = new StringBuffer();
		try{
			if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
                rst.beforeFirst();
            }
			ResultSetMetaData rsmd = rst.getMetaData();
			int numCol = rsmd.getColumnCount();
			int totalHeaderLenght = 0;
			for(int x = 1; x <= numCol; x++){
				String columnName = "     " + rsmd.getColumnLabel(x) + "     ";
				totalHeaderLenght += columnName.length();
				buffer.append(columnName);
			}
			
			buffer.append('\n');
			for(int x = 0; x <= totalHeaderLenght; x++){
				buffer.append("-");
			}
			buffer.append('\n');
			
			boolean more = rst.next();
			while(more){
				for(int x = 1; x <= numCol; x++){
					buffer.append("     "+rst.getString(x)+"     ");
				}
				buffer.append('\n');
				more=rst.next();
			}
		}catch(SQLException exp){
			exp.getStackTrace();
		}
		
		System.out.println(buffer.toString());
	}
	
	/**
	 * 
	 * @param o
	 */
	public void displayCollection(Object o){
		System.out.println(toString(o));
	}
	
	public String toString(Object o){
		StringBuffer buffer = new StringBuffer();
		if(o instanceof List){
			List<?> ox = (List<?>)o;
			for(Object x : ox){
				buffer.append(x.toString() + ";");
				buffer.append('\n');
			}
			
		}else if(o instanceof Map){
			Map<?,?> ox = (Map<?,?>)o;
			buffer.append(ox.toString());
		}else if(o instanceof Set){
			Set<?> ox = (Set<?>)o;
			buffer.append(ox.toString());
		}else if(o instanceof Table) {
			return toString(((Table)o).getRows());
		}
		return buffer.toString();
	}
	
////////////////////////////////////Block Of Queries///////////////////////
	
	 
	public boolean executeTableManipulation(String query)
    throws SQLException,Exception{
		
		if(query == null 
				|| query.length() <=0 
				|| !query.trim().toLowerCase().startsWith("create")
				|| !query.trim().toLowerCase().startsWith("delete")
				|| !query.trim().toLowerCase().startsWith("alter")){
			throw new Exception("Bad Formated Query : " + query);
		}
    	
        boolean isCreated = false;
        PreparedStatement stmt = null;
        try{ 
            if(conn != null){
            	//
                stmt = conn.prepareStatement(query);
                stmt.executeUpdate();
                isCreated = true;
            }            
        }catch(SQLException exp){
        	throw exp;
        }finally{
        	if(stmt != null) stmt.close();
        }
        return isCreated;		
    }
	
	
    /**
     * Query for Update,Insert,Delete
     * @param conn
     * @param query
     * @return Number Of affected rows
     */
    public ResultSet executeCRUDQuery(String query)
    throws SQLException,Exception{
    	
    	if(query == null 
				|| query.length() <=0){
			throw new Exception("Bad Formated Query : " + query);
		}
    	
    	if(query.trim().toLowerCase().startsWith("insert")
				|| query.trim().toLowerCase().startsWith("update")
				|| query.trim().toLowerCase().startsWith("delete")) {    		
            PreparedStatement stmt = null;
            try{ 
                if(conn != null){
                    stmt = conn.prepareStatement(query);
                    int rowUpdate = stmt.executeUpdate();
                    System.out.println("rows effected " + (rowUpdate == 0 ? "NO" : "YES"));
                }            
            }catch(SQLException exp){
                throw exp;
            }finally{
            	if(stmt != null)
            		stmt.close();
            }
            return null;
    	}else {
    		return executeSelect(query);
    	}	
    }
    
    /**
     * 
     * @param query
     * @param setParameter
     * @return
     * @throws SQLException
     * @throws Exception
     */
    @Deprecated
    public int executeUpdate(SQLUpdateQuery query
    		, Row setParameter)
    throws SQLException,Exception{
    	
    	if(setParameter == null 
    			|| setParameter.size() <= 0){
    		throw new Exception("Set Parameter Should not be bull or empty!!!");
		}
    	
        int rowUpdated = 0;
        PreparedStatement stmt=null;
        String queryStr = query.toString();
        String [] whereKeySet = query.getWhereProperties().getKeys();
        
        try{ 
            if(conn != null){
                stmt = conn.prepareStatement(queryStr);
                
                int length = setParameter.size();
                stmt = bindValueToStatement(stmt, 1, setParameter.getKeys(), setParameter.keyValueMap());
                if(whereKeySet != null)
                	stmt = bindValueToStatement(stmt, length+1, whereKeySet, query.getWhereProperties().keyValueMap());
                
                rowUpdated = stmt.executeUpdate();
                 
            }            
        }catch(SQLException exp){
            throw exp;
        }catch (IllegalArgumentException e) {
            throw e;
		}finally{
        	if(stmt != null)
        		stmt.close();
        }
        return rowUpdated;		
    }
    
    /**
     * 
     * @param query
     * @return
     * @throws SQLException
     * @throws Exception
     */
    public int executeUpdate(SQLUpdateQuery query) throws SQLException,Exception{
    	
    	Row setProperties = query.getRow();
    	if(setProperties == null 
    			|| setProperties.size() <= 0){
    		throw new Exception("Set Parameter Should not be bull or empty!!!");
		}
    	
        int rowUpdated = 0;
        PreparedStatement stmt=null;
        String queryStr = query.toString();
        String [] whereKeySet = query.getWhereProperties().getKeys();
        
        try{ 
            if(conn != null){
                stmt = conn.prepareStatement(queryStr);
                
                int length = setProperties.size();
                stmt = bindValueToStatement(stmt, 1, setProperties.getKeys(), setProperties.keyValueMap());
                if(whereKeySet != null)
                	stmt = bindValueToStatement(stmt, length+1, whereKeySet, query.getWhereProperties().keyValueMap());
                
                rowUpdated = stmt.executeUpdate();
            }            
        }
        catch(SQLException exp) { throw exp;}
        catch (IllegalArgumentException e) { throw e;}
        finally{
        	if(stmt != null) stmt.close();
        }
        return rowUpdated;		
    }
    
    private Row getLeastAppropriateProperties(List<Row> items, int index){
    	if(items == null || items.isEmpty()){
    		return new Row();
    	}
    	if(index < items.size()){
    		return items.get(index);
    	}else{
    		return items.get(0);
    	}
    }
    
    /**
     * 
     * @param batchSize
     * @param queryC
     * @param updateProperties
     * @param whereClause
     * @return
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws Exception
     */
    public Integer[] executeBatchUpdate(int batchSize
    		, SQLUpdateQuery queryC
    		, List<Row> updateProperties
    		, List<Row> whereClause)
    throws SQLException,IllegalArgumentException,Exception{
    	
    	if(updateProperties == null 
    			|| updateProperties.size() <= 0){
    		throw new Exception("Set Parameter Should not be bull or empty!!!");
		}
    	
    	List<Integer> affectedRows = new ArrayList<Integer>();
        PreparedStatement stmt = null;
        String query = queryC.toString();
        
        try{ 
        	batchSize = (batchSize < 100) ? 100 : batchSize;//Least should be 100
            if(conn != null){
                //
                List<int[]> batchUpdatedRowsCount = new ArrayList<int[]>();
                
                stmt = conn.prepareStatement(query);
        		int batchCount = 1;
        		for (int index = 0; index < updateProperties.size(); index++) {
        			
        			String[] keySet = updateProperties.get(index).getKeys();
					Map<String, Property> row = updateProperties.get(index).keyValueMap();
					stmt = bindValueToStatement(stmt, 1, keySet, row);
					
					int length = keySet.length;
					Row whereClouseProperties = getLeastAppropriateProperties(whereClause, index);
					String[] whereKeySet = whereClouseProperties.getKeys();
					Map<String, Property> rowWhere = whereClouseProperties.keyValueMap();
					stmt = bindValueToStatement(stmt, length + 1, whereKeySet, rowWhere);
					
					stmt.addBatch();
					if ((++batchCount % batchSize) == 0) {
						batchUpdatedRowsCount.add(stmt.executeBatch());
					}
				}
				if(updateProperties.size() % batchSize != 0)
        			batchUpdatedRowsCount.add(stmt.executeBatch());
        		
        		for (int[] rr  : batchUpdatedRowsCount) {
            		for(int i = 0; i < rr.length ; i++){
                		affectedRows.add(rr[i]);
                	}
				}
        		
            }            
        }catch(SQLException exp){
            throw exp;
        }catch(IllegalArgumentException iel){
        	throw iel;
        }finally{
        	if(stmt != null){
        		try {
					stmt.clearBatch();
				} catch (Exception e) {
					e.printStackTrace();
				}
        		stmt.close();
        	}
        }
        return affectedRows.toArray(new Integer[]{});		
    }
    
    /**
     * 
     * @param dQuery
     * @return
     * @throws SQLException
     * @throws Exception
     */
    public int executeDelete(SQLDeleteQuery dQuery)
    throws SQLException,Exception{
    	
    	if(dQuery.getWhereParamExpressions() == null || dQuery.getWhereParamExpressions().size() <= 0){
    		throw new Exception("Where parameter should not be null or empty!!!");
    	}
    	
        int rowUpdated = 0;
        PreparedStatement stmt=null;
        String query = dQuery.toString();
        try{ 
            if(conn != null){
                stmt = conn.prepareStatement(query);
                stmt = bindValueToStatement(stmt, 1, dQuery.getWhereParams(), dQuery.getWhereProperties().keyValueMap());
                rowUpdated = stmt.executeUpdate();
            }            
        }catch(SQLException exp){
            throw exp;
        }catch (IllegalArgumentException e) {
            throw e;
		}finally{
        	if(stmt != null) stmt.close();
        }
        return rowUpdated;		
    }
    
    /**
     * 
     * @param batchSize
     * @param dQuery
     * @param whereClause
     * @return
     * @throws SQLException
     * @throws Exception
     */
    public int executeBatchDelete(int batchSize
    		, SQLDeleteQuery dQuery
    		, List<Row> whereClause)
    throws SQLException,Exception{
    	
    	if(dQuery.getWhereParams() == null || dQuery.getWhereParams().length <= 0){
    		throw new Exception("Where parameter should not be null or empty!!!");
    	}
    	
        int rowUpdated = 0;
        PreparedStatement stmt=null;
        String query = dQuery.toString();
        String[] whereKeySet = whereClause.get(0).getKeys();
        try{
        	batchSize = (batchSize < 100) ? 100 : batchSize;//Least should be 100
            if(conn != null){
            	//
                int batchCount = 1;
                stmt = conn.prepareStatement(query);
            	for (Row paramValue: whereClause) {
            		
                    stmt = bindValueToStatement(stmt, 1, whereKeySet, paramValue.keyValueMap());
                    stmt.addBatch();
					if ((++batchCount % batchSize) == 0) {
						stmt.executeBatch();
					}
				}
            	if(whereClause.size() % batchSize != 0)
            		stmt.executeBatch();
            	 
            }            
        }catch(SQLException exp){
            throw exp;
        }catch (IllegalArgumentException e) {
            throw e;
		}finally{
			if(stmt != null){
        		try {
					stmt.clearBatch();
				} catch (Exception e) {
					e.printStackTrace();
				}
        		stmt.close();
        	}
        }
        return rowUpdated;		
    }
    
    /**
     * Query for Insert with Auto Generated Id
     * @param conn
     * @param query
     * @return Last Inserted ID
     */
    public int executeInsert(boolean isAutoGenaretedId
    		, String query)
    throws SQLException,IllegalArgumentException{
    	
    	int lastIncrementedID = 0;
    	PreparedStatement stmt=null;
        try{
        	if(query != null 
	    			&& query.length() > 0 
	    			&& !query.toUpperCase().startsWith("INSERT")){
	    		throw new IllegalArgumentException("Query string must be a Insert query!");
	    	}
            if(conn != null){
                if (isAutoGenaretedId) {
					stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
					stmt.executeUpdate();
					ResultSet rs = stmt.getGeneratedKeys();
					if (rs != null && rs.next())
						lastIncrementedID = rs.getInt(1);
				}else{
					stmt = conn.prepareStatement(query);                	
					lastIncrementedID = stmt.executeUpdate();
				}
            }            
        }catch(SQLException exp){
            throw exp;
        }catch(IllegalArgumentException iel){
        	throw iel;
        }finally{
        	if(stmt != null) stmt.close();
        }
        return lastIncrementedID;		
    }
    
    /**
     * 
     * @param isAutoGenaretedId
     * @param iQuery
     * @return
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws Exception
     */
    
    public int executeInsert(boolean isAutoGenaretedId, SQLInsertQuery iQuery)
    throws SQLException,IllegalArgumentException,Exception{
    	
    	if(iQuery.getColumns() == null || iQuery.getColumns().length <= 0){
    		throw new Exception("Parameter should not be null or empty!!!");
    	}
    	
    	int affectedRows = 0;
        PreparedStatement stmt=null;
        String query = iQuery.toString();
        
        try{ 
            if(conn != null){
            	if(isAutoGenaretedId){
            		stmt = conn.prepareStatement(query,	Statement.RETURN_GENERATED_KEYS);
                	stmt = bindValueToStatement(stmt, 1,iQuery.getRow().getKeys(), iQuery.getRow().keyValueMap());
                	stmt.executeUpdate();
                	ResultSet set = stmt.getGeneratedKeys();
                	if(set != null && set.next()){
                		affectedRows = set.getInt(1);
                	}                	
            	}else{
            		stmt = conn.prepareStatement(query);
                	stmt = bindValueToStatement(stmt, 1, iQuery.getRow().getKeys(), iQuery.getRow().keyValueMap());
                	affectedRows = stmt.executeUpdate();
            	}
            }            
        }catch(SQLException exp){
            throw exp;
        }catch(IllegalArgumentException iel){
        	throw iel;
        }finally{
        	if(stmt != null) stmt.close();
        }
        return affectedRows;		
    }
    
    /**
     * 
     * @param isAutoGenaretedId
     * @param batchSize
     * @param iQuery
     * @param params
     * @return
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws Exception
     */
    public Integer[] executeBatchInsert(boolean isAutoGenaretedId
    		, int batchSize
    		, String tableName
    		, List<Row> params)
    throws SQLException,IllegalArgumentException,Exception{
    	
    	if(params == null || params.size() <= 0){
    		throw new Exception("Parameter should not be null or empty!!!");
    	}
    	
    	List<Integer> affectedRows = new ArrayList<Integer>();
        PreparedStatement stmt=null;
        /**
         * Object[] keySet = iQuery.getProperties().getKeys();
         * String query = iQuery.toString();
         */
        Object[] keySet = params.get(0).getKeys();
        List<Property> nValues = new ArrayList<Property>();
        for (Property property : params.get(0).getCloneProperties()) {
			nValues.add(new Property(property.getKey()));
		}
        Property[] values = (Property[]) nValues.toArray(new Property[0]);
        SQLInsertQuery iQuery = (SQLInsertQuery) new SQLQuery.Builder(QueryType.INSERT).into(tableName).values(values).build();
        String query = iQuery.toString();
        
        try{ 
        	batchSize = (batchSize < 100) ? 100 : batchSize;//Least should be 100
            if(conn != null){
                //
            	if(isAutoGenaretedId){
            		stmt = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
                	int batchCount = 1;
            		for (Row row : params) {
                		stmt = bindValueToStatement(stmt, 1, keySet, row.keyValueMap());
                		stmt.addBatch();
                		if((++batchCount % batchSize) == 0){
                			stmt.executeBatch();
                		}
					}
            		if(params.size() % batchSize != 0)
            			stmt.executeBatch();
        			
                	ResultSet set = stmt.getGeneratedKeys();
                	
                	while(set.next()){
                		affectedRows.add(set.getInt(1));
                	}                	
            	}else{
            		stmt = conn.prepareStatement(query);
            		int batchCount = 1;
            		List<int[]> batchUpdatedRowsCount = new ArrayList<int[]>();
            		for (Row row : params) {
                		stmt = bindValueToStatement(stmt, 1, keySet, row.keyValueMap());
                		stmt.addBatch();
                		if((++batchCount % batchSize) == 0){
                			batchUpdatedRowsCount.add(stmt.executeBatch());
                		}
					}
            		if(params.size() % batchSize != 0)
            			batchUpdatedRowsCount.add(stmt.executeBatch());
        			
            		for (int[] rr  : batchUpdatedRowsCount) {
                		for(int i = 0; i < rr.length ; i++){
                    		affectedRows.add(rr[i]);
                    	}
    				}
            	}
            }            
        }catch(SQLException exp){
            throw exp;
        }catch(IllegalArgumentException iel){
        	throw iel;
        }finally{
        	if(stmt != null){
        		try {
					stmt.clearBatch();
				} catch (Exception e) {
					e.printStackTrace();
				}
        		stmt.close();
        	}
        }
        return affectedRows.toArray(new Integer[]{});		
    }
    
    /**
     * 
     * @param conn
     * @param query
     * @return
     * @throws SQLException
     */
    public int getScalerValue(String query)
    throws SQLException{
    	
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        int rowCount = 0;
        try{
            if(conn != null){
                pstmt = conn.prepareStatement(query);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    rowCount = rs.getInt(1);                   
                } else {
                     rowCount=0;
                }
            }            
        }catch(SQLException e){
            throw e;
        }finally{
        	if(pstmt != null)
        		pstmt.close();
        }
        return rowCount;
     }
    
    /**
     * 
     * @param cQuery
     * @return
     * @throws SQLException
     */
    public int getScalerValue(SQLScalerQuery cQuery)
    throws SQLException{
    	
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        int rowCount = 0;
        String query = cQuery.toString();
        Row whereClause = cQuery.getWhereProperties();
        try{
            if(conn != null){
                pstmt = conn.prepareStatement(query);
                
                pstmt = bindValueToStatement(pstmt
                		, 1
                		, whereClause.getKeys()
                		, whereClause.keyValueMap());
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    rowCount = rs.getInt(1);                   
                } else {
                     rowCount=0;
                }
            }            
        }catch(SQLException e){
            throw e;
        }finally{
        	if(pstmt != null)
        		pstmt.close();
        }
        return rowCount;
     }
    
    /**
     * Query for select
     * @param conn
     * @param query
     * @return ResultSet
     */
    public ResultSet executeSelect(String query)
    throws SQLException,IllegalArgumentException{
    	
        PreparedStatement stmt = null;
        ResultSet rst=null;
        try{
        	
        	if(query != null 
	    			&& query.length() > 0 
	    			&& !query.toUpperCase().startsWith("SELECT")){
	    		throw new IllegalArgumentException("Query string must be a Select query!");
	    	}
            if(conn != null){
            	stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                rst = stmt.executeQuery();                 
            }            
        }catch(SQLException exp){            
            throw exp;
        }catch(IllegalArgumentException iel){
        	throw iel;
        }finally{
        	getStatementHolder().add(stmt);
        }
        return rst;           
    }
    
    /**
     * 
     * @param query
     * @return
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    
    public ResultSet executeSelect(SQLSelectQuery query)
    throws SQLException,IllegalArgumentException{
    	
        PreparedStatement stmt = null;
        ResultSet rst=null;
        String queryStr = query.toString();
        Row whereClause = query.getWhereProperties();
        try{
            if(conn != null && !conn.isClosed()){
            	stmt = conn.prepareStatement(queryStr,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
            	stmt = bindValueToStatement(stmt, 1, whereClause.getKeys(), whereClause.keyValueMap());
        		rst = stmt.executeQuery();
            }            
        }catch(SQLException exp){            
            throw exp;
        }catch(IllegalArgumentException iel){
        	throw iel;
        }finally{
        	getStatementHolder().add(stmt);
        }
        return rst;           
    }
    
    public Table collection(ResultSet rst, String...columns){
    	if(columns.length == 0) {
    		return collection(rst);
    	}
    	Table result = new Table();
    	try{
    		//IF cursor is moved till last row. Then set to the above first row. 
    		if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
    			rst.beforeFirst();
    		}
    		ResultSetMetaData rsmd = rst.getMetaData();
    		//Optimization
    		List<Integer> columnIndecies = new ArrayList<Integer>();
    		for(String columnName : columns){
    			columnIndecies.add(rst.findColumn(columnName));
    		}

    		while(rst.next()){ //For each Row
    			Row row = new Row();
    			for(int x : columnIndecies){ //For each column in the columns
    				String key = rsmd.getColumnName(x);
    				DataType type = convertDataType(rsmd
    						.getColumnTypeName(x));
    				Object value = getValueFromResultSet(type, rst, x);
    				Property property = new Property(key, value, type);
    				row.add(property);
    			}
    			if(row.size() > 0)
    				result.add(row);
    		}
    	}catch(SQLException exp){
    		result = null;
    		exp.getStackTrace();
    	}
    	return result;
    }
	
	/**
	 * 
	 * @param rst
	 * @return
	 */
    public Table collection(ResultSet rst){
    	Table result = new Table();
		try{
			//IF cursor is moved till last row. Then set to the above first row. 
			if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
                rst.beforeFirst();
            }
			ResultSetMetaData rsmd = rst.getMetaData();
			int numCol = rsmd.getColumnCount();
			
			while(rst.next()){ //For each Row
				Row row = new Row();
				for(int x = 1; x <= numCol; x++){ //For each column in a Row
					String key = rsmd.getColumnName(x);
					DataType type = convertDataType(rsmd.getColumnTypeName(x));
					Object value = getValueFromResultSet(type, rst, x);
					
					Property property = new Property(key, value, type);
					row.add(property);
				}
				result.add(row);
			}
		}catch(SQLException exp){
			result = null;
			exp.getStackTrace();
		}
		return result;
	}
	
	public List<Row> convertToLists(ResultSet rst){
		List<Row> result = new ArrayList<Row>();
		try{
			
			//IF cursor is moved till last row. Then set to the above first row. 
			if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
                rst.beforeFirst();
            }
			
			ResultSetMetaData rsmd = rst.getMetaData();
			int numCol = rsmd.getColumnCount();
			
			while(rst.next()){ //For each Row
				Row row = new Row();
				for(int x = 1; x <= numCol; x++){ //For each column in a Row
					
					String key = rsmd.getColumnName(x);
					DataType type = convertDataType(rsmd.getColumnTypeName(x));
					Object value = getValueFromResultSet(type, rst, x);
					
					Property property = new Property(key, value, type);
					row.add(property);
				}
				result.add(row);
			}
		}catch(SQLException exp){
			result = null;
			exp.getStackTrace();
		}
		
		return result;
	}
	
	public List<Row> convertToLists(ResultSet rst, String...columns){
		if(columns.length == 0){
            return convertToLists(rst);
        }
		List<Row> result = new ArrayList<Row>();
		try{
			//IF cursor is moved till last row. Then set to the above first row. 
			if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
                rst.beforeFirst();
            }
			
			ResultSetMetaData rsmd = rst.getMetaData();
			//Optimization
            List<Integer> columnIndecies = new ArrayList<Integer>();
            for(String columnName : columns){
                columnIndecies.add(rst.findColumn(columnName));
            }
			
			while(rst.next()){ //For each Row
				Row row = new Row();
				for(int x : columnIndecies){ //For each column in the paramProperties
					String key = rsmd.getColumnName(x);
					DataType type = convertDataType(rsmd
							.getColumnTypeName(x));
					Object value = getValueFromResultSet(type, rst, x);
					Property property = new Property(key, value, type);
					row.add(property);
				}
				if(row.size() > 0)
					result.add(row);
			}
		}catch(SQLException exp){
			result = null;
			exp.getStackTrace();
		}
		return result;
	}
	
	public List<Map<String, Object>> convertToKeyValuePaire(ResultSet rst){
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		
		try{
			
			//IF cursor is moved till last row. Then set to the above first row. 
			if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
                rst.beforeFirst();
            }
			
			ResultSetMetaData rsmd = rst.getMetaData();
			int numCol = rsmd.getColumnCount();
			
			while(rst.next()){ //For each Row
				
				Map<String, Object> row = new HashMap<String, Object>(numCol);
				for(int x = 1; x <= numCol; x++){ //For each column in a Row
					
					String key = rsmd.getColumnName(x);
					DataType type = convertDataType(rsmd.getColumnTypeName(x));
					Object value = getValueFromResultSet(type, rst, x);
					
					row.put(key, value);
				}
				result.add(row);
			}
		}catch(SQLException exp){
			
			result = null;
			exp.getStackTrace();
		}
		
		return result;
	}
	
	public List<Map<String, Object>> convertToKeyValuePaire(ResultSet rst, List<String> paramProperties){
		
		if(paramProperties == null || paramProperties.size() <= 0){
            return convertToKeyValuePaire(rst);
        }
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		
		try{
			
			//IF cursor is moved till last row. Then set to the above first row. 
			if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
                rst.beforeFirst();
            }
			
			ResultSetMetaData rsmd = rst.getMetaData();
			//Optimization
            List<Integer> columnIndecies = new ArrayList<Integer>();
            for(String columnName : paramProperties){
                columnIndecies.add(rst.findColumn(columnName));
            }
			
			while(rst.next()){ //For each Row
				
				Map<String, Object> row = new HashMap<String, Object>(columnIndecies.size());
				for(int x : columnIndecies){ //For each column in paramProperties

					String key = rsmd.getColumnName(x);

					DataType type = convertDataType(rsmd
							.getColumnTypeName(x));
					Object value = getValueFromResultSet(type, rst, x);
					row.put(key, value);
				}
				if(row.size() > 0)
					result.add(row);
			}
		}catch(SQLException exp){
			
			result = null;
			exp.getStackTrace();
		}
		
		return result;
	}
	
	public List<Map<String, Object>> convertToKeyValuePaire(ResultSet rst, List<String> paramProperties, List<String> paramPropertyNames){
		
		if(paramProperties == null 
				|| paramProperties.size() <= 0){
			return convertToKeyValuePaire(rst);
		}
		if(paramPropertyNames == null
				|| paramPropertyNames.size() <= 0
				|| paramProperties.size() != paramPropertyNames.size()){
			return convertToKeyValuePaire(rst, paramProperties);
		}
		
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		
		try{
			
			if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
				rst.beforeFirst();
			}
			
			ResultSetMetaData rsmd = rst.getMetaData();
			List<Integer> columnIndices = new ArrayList<Integer>(paramProperties.size());
			for(String columnName : paramProperties){
				columnIndices.add(rst.findColumn(columnName));
			}
			
			while(rst.next()){ //For each Row
				
				HashMap<String, Object> row = new HashMap<String, Object>(columnIndices.size());
				int newNameCount = 0;
				for(int x : columnIndices){ //For each column in paramProperties
					
					String key = paramPropertyNames.get(newNameCount++);
					DataType type = convertDataType(rsmd.getColumnTypeName(x));
					Object value = getValueFromResultSet(type, rst, x);
					
					row.put(key, value);
				}
				result.add(row);
			}
		}catch(SQLException exp){
			
			result = null;
			exp.getStackTrace();
		}
		
		return result;
	}
	
	public Map<Object, Map<String, Object>> convertToIndexedKeyValuePaire(ResultSet rst, String indexColumn){
		
		Map<Object,Map<String, Object>> result = new HashMap<Object, Map<String,Object>>();
		
		try{
			
			//IF cursor is moved till last row. Then set to the above first row. 
			if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
                rst.beforeFirst();
            }
			
			ResultSetMetaData rsmd = rst.getMetaData();
			int numCol = rsmd.getColumnCount();
			
			while(rst.next()){ //For each Row
				
				Object indexColValue = null;
				
				Map<String, Object> row = new HashMap<String, Object>(numCol);
				for(int x = 1; x <= numCol; x++){ //For each column in a Row
					
					String key = rsmd.getColumnName(x);
					DataType type = convertDataType(rsmd.getColumnTypeName(x));
					Object value = getValueFromResultSet(type, rst, x);
					
					if(key.equals(indexColumn))
						indexColValue = value;
					
					row.put(key, value);
				}
				if(indexColValue != null
						&& !result.containsKey(indexColValue))
					result.put(indexColValue,row);
				
			}
		}catch(SQLException exp){
			
			result = null;
			exp.getStackTrace();
		}
		
		return result;
	}
	
	public Map<Object, Map<String, Object>> convertToIndexedKeyValuePaire(ResultSet rst, String indexColumn, List<String> paramProperties){
		
		if(paramProperties == null || paramProperties.size() <= 0){
            return convertToIndexedKeyValuePaire(rst, indexColumn);
        }
		
		Map<Object,Map<String, Object>> result = new HashMap<Object, Map<String,Object>>();
		
		try{
			
			//IF cursor is moved till last row. Then set to the above first row. 
			if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
                rst.beforeFirst();
            }
			
			ResultSetMetaData rsmd = rst.getMetaData();
			
			//Optimization
            List<Integer> columnIndecies = new ArrayList<Integer>();
            for(String columnName : paramProperties){
                columnIndecies.add(rst.findColumn(columnName));
            }
			
			while(rst.next()){ //For each Row
				
				Object indexColValue = null;
				
				Map<String, Object> row = new HashMap<String, Object>(columnIndecies.size());
				for(int x : columnIndecies){ //For each column in the paramProperties
					
					String key = rsmd.getColumnName(x);

					DataType type = convertDataType(rsmd
							.getColumnTypeName(x));
					Object value = getValueFromResultSet(type, rst, x);
					if (key.equals(indexColumn))
						indexColValue = value;
					row.put(key, value);

				}
				if(indexColValue != null
						&& !result.containsKey(indexColValue)
						&& row.size() > 0)
					result.put(indexColValue,row);
				
			}
		}catch(SQLException exp){
			
			result = null;
			exp.getStackTrace();
		}
		
		return result;
	}
	
	public Map<Object, Map<String, Object>> convertToIndexedKeyValuePaire(ResultSet rst, String indexColumn, List<String> paramProperties, List<String> paramPropertyNames){
		
		if(paramProperties == null 
				|| paramProperties.size() <= 0){
			return convertToIndexedKeyValuePaire(rst, indexColumn);
		}
		if(paramPropertyNames == null
				|| paramPropertyNames.size() <= 0
				|| paramProperties.size() != paramPropertyNames.size()){
			return convertToIndexedKeyValuePaire(rst, indexColumn, paramProperties);
		}
		
		Map<Object,Map<String, Object>> result = new HashMap<Object, Map<String, Object>>();
		
		try{
			
			if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
				rst.beforeFirst();
			}
			
			ResultSetMetaData rsmd = rst.getMetaData();
			List<Integer> columnIndices = new ArrayList<Integer>();
			for(String columnName : paramProperties){
				columnIndices.add(rst.findColumn(columnName));
			}
			
			while(rst.next()){ //For each Row
				
				Object indexColValue = null;
				
				HashMap<String, Object> row = new HashMap<String, Object>(columnIndices.size());
				int newNameCount = 0;
				for(int x : columnIndices){ //For each column in paramProperties
					
					String key = rsmd.getColumnName(x);
					String keyConverted = paramPropertyNames.get(newNameCount++);
					DataType type = convertDataType(rsmd.getColumnTypeName(x));
					Object value = getValueFromResultSet(type, rst, x);
					
					if(key.equals(indexColumn))
						indexColValue = value;
					
					row.put(keyConverted, value);
				}
				if(indexColValue != null
						&& !result.containsKey(indexColValue))
					result.put(indexColValue,row);
				
			}
		}catch(SQLException exp){
			
			result = null;
			exp.getStackTrace();
		}
		
		return result;
	}
	
	/**
	 * 
	 * @param rst
	 * @param rowIndex > 0
	 * @param isObjectType
	 * @return
	 */
	
	public Row retrieveRow(ResultSet rst, int rowIndex){
        Row result = null;
        try{
            ResultSetMetaData rsmd = rst.getMetaData();
            int numCol = rsmd.getColumnCount();
            if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE){
            	
                int offset = (rowIndex <= 0) ? 1 : rowIndex;
                rst.absolute(offset);
                Row row = new Row();
                
                for(int x = 1; x <= numCol; x++){ //For each column in a Row
                    String key = rsmd.getColumnName(x);
                    DataType type = convertDataType(rsmd.getColumnTypeName(x));
                    Object value = getValueFromResultSet(type, rst, x);
                    Property property = new Property(key, value, type);
                    row.add(property);
                }
                result = row;
            }else{
                if(!rst.isAfterLast()){
                    while(rst.next()){
                        if(rowIndex == rst.getRow()){
                            Row row = new Row();
                            for(int x = 1; x <= numCol; x++){ //For each column in a Row
                                String key = rsmd.getColumnName(x);
                                DataType type = convertDataType(rsmd.getColumnTypeName(x));
                                Object value = getValueFromResultSet(type, rst, x);
                                Property property = new Property(key, value, type);
                                row.add(property);
                            }
                            result = row;
                            break;
                        }
                    }//end while
                }
            }//
        }catch(SQLException exp){
            result = null;
            exp.getStackTrace();
        }
        return result;
    }
	
	public Row retrieveColumn(ResultSet rst, String indexColumn){
		
		Row result = new Row();
		
		try{
			
			//IF cursor is moved till last row. Then set to the above first row. 
			if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
                rst.beforeFirst();
            }
			
			ResultSetMetaData rsmd = rst.getMetaData();
			int x = (rst.findColumn(indexColumn) <= 0) ? 1 : rst.findColumn(indexColumn);
			
			String key = rsmd.getColumnName(x);
			DataType type = convertDataType(rsmd.getColumnTypeName(x));
			
			while(rst.next()){ //For each Row
				Object value = getValueFromResultSet(type, rst, x);
				Property prop = new Property(key,value, type);
				result.add(prop);
			}
		}catch(SQLException exp){
			result = null;
			exp.getStackTrace();
		}
		return result;
	}
	
	public Row retrieveColumn(ResultSet rst, int indexColumn){
		
		Row result = new Row();
		
		try{
			//IF cursor is moved till last row. Then set to the above first row. 
			if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
                rst.beforeFirst();
            }
			
			ResultSetMetaData rsmd = rst.getMetaData();
			int numCol = rsmd.getColumnCount();
			
			int x = 1;
			if( 1 <= indexColumn && indexColumn <= numCol){
				x = indexColumn;
			}
			
			String key = rsmd.getColumnName(x);
			DataType type = convertDataType(rsmd.getColumnTypeName(x));
			
			while(rst.next()){ //For each Row
				Object value = getValueFromResultSet(type, rst, x);
				Property prop = new Property(key,value, type);
				result.add(prop);
			}
		}catch(SQLException exp){
			result = null;
			exp.getStackTrace();
		}
		return result;
	}
	
	public Blob createBlob(String val) throws SQLException {
		byte[] bytes = val.getBytes();
		Blob blob = conn.createBlob();
		blob.setBytes(1, bytes);
		return blob;
	}
	
	/*>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Private Methods>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>*/
	
	private PreparedStatement bindValueToStatement(PreparedStatement stmt
			, int startIndex
			, Object[] params
			, Map<String, Property> paramValues)
    throws SQLException,IllegalArgumentException{
    	
    	try{
    		if(params.length == paramValues.size()){
            	
            	if(stmt != null){
            		int index = 1;
            		if(startIndex > 0){
            			index = startIndex;
            		}else{
            			throw new IllegalArgumentException("Index Out Of Bound!!!");
            		}
            		for (Object param : params) {
            			
            			Property property = paramValues.get(param.toString());

            			switch (property.getType()) {
	            			case STRING:
	            				stmt.setString(index++, (property.getValue() != null) ? property.getValue().toString().trim() : null);
	            				break;
	            			case INT:
	            				if(property.getValue() != null){
	            					stmt.setInt(index++, (Integer)property.getValue());
	            				}else{
	            					stmt.setNull(index++, java.sql.Types.INTEGER);
	            				}
	            				break;
	            			case BOOL:
	            				if(property.getValue() != null){
                                    stmt.setBoolean(index++, (Boolean)property.getValue());
                                }else{
                                    stmt.setNull(index++, java.sql.Types.BOOLEAN);
                                }
	            				break;
	            			case FLOAT:
	            				if(property.getValue() != null){
	            					stmt.setFloat(index++, (Float)property.getValue());
	            				}else{
	            					stmt.setNull(index++, java.sql.Types.FLOAT);
	            				}
	            				break;
	            			case DOUBLE:
	            				if(property.getValue() != null){
	            					stmt.setDouble(index++, (Double)property.getValue());
	            				}else{
	            					stmt.setNull(index++, java.sql.Types.DOUBLE);
	            				}
	            				break;
	            			case SQLDATE:
	            			case SQLTIMESTAMP:
	            				if(property.getValue() != null) {
	            					if(property.getValue() instanceof java.sql.Timestamp) {
	            						stmt.setTimestamp(index++, (Timestamp)property.getValue());
	            					}else if(property.getValue() instanceof java.sql.Time) {
	            						stmt.setTime(index++, (Time)property.getValue());
	            					}else {
	            						stmt.setDate(index++, (Date)property.getValue());
	            					}
	            				}else {
	            					stmt.setNull(index++, java.sql.Types.DATE);
	            				}
	            				break;
	            			case BLOB:
	            				if(property.getValue() != null && property.getValue() instanceof Blob){
	            					stmt.setBlob(index++, (Blob)property.getValue());
	            				}else if(property.getValue() != null && property.getValue() instanceof String){
	            					byte[] bytes = property.getValue().toString().getBytes();
	            					Blob blob = conn.createBlob();
	            					blob.setBytes(1, bytes);
	            					stmt.setBlob(index++, blob);
	            				}
	            				else{
	            					stmt.setNull(index++, java.sql.Types.BLOB);
	            				}
	            				break;
	            			case BYTEARRAY:
	            				if(property.getValue() != null && property.getValue() instanceof Byte){
                                    stmt.setBytes(index++, (byte[])property.getValue());
                                }else if(property.getValue() != null && property.getValue() instanceof String){
                                    stmt.setBytes(index++, ((String)property.getValue()).getBytes());
                                }else if(property.getValue() != null){
                                    stmt.setBytes(index++, (byte[])property.getValue());
                                }else{
                                    stmt.setNull(index++, java.sql.Types.ARRAY);
                                }
	            				break;
	            			default:
	            				stmt.setObject(index++, property.getValue());
	            				break;
            			}
            		}
            	}
            }else{
            	throw new IllegalArgumentException("Parameter length mismatch");
            }
    	}catch(SQLException exp){
    		throw exp;
    	}
    	return stmt;
    }
	
	private Object getValueFromResultSet(DataType type, ResultSet rst, int index)
	throws SQLException{
		
		Object value = null;
		switch (type) {

		case INT:
			value = new Integer(rst.getInt(index));
			break;
		case DOUBLE:
			value = new Double(rst.getDouble(index));
			break;
		case FLOAT:
			value = new Float(rst.getFloat(index));
			break;
		case STRING:
			value = rst.getString(index);
			break;
		case BOOL:
			value = new Boolean(rst.getBoolean(index));
			break;
		case SQLDATE:
			value = rst.getDate(index);
			break;
		case SQLTIMESTAMP:
			value = rst.getTimestamp(index);
			break;
		case BYTEARRAY:
			byte[] arr = rst.getBytes(index); 
			value = arr;
			break;
		default:
			value = rst.getObject(index);
			break;
		}
		
		return value;
	}
	
	private DataType convertDataType(String type){
		
		String trimedType = type.trim().toUpperCase();
		
		if(trimedType.equals("CHAR") 
				|| trimedType.equals("VARCHAR")
				|| trimedType.equals("LONGVARCHAR")){
			
			return DataType.STRING;
			
		}
		else if(trimedType.equals("INTEGER") 
				|| trimedType.equals("BIGINT")
				|| trimedType.equals("SMALLINT")){
			return DataType.INT;
			
		}
		else if(trimedType.equals("DATE")
				|| trimedType.equals("DATETIME")){
			return DataType.SQLDATE;
			
		}else if(trimedType.equals("TIME")
				|| trimedType.equals("TIMESTAMP")){
			return DataType.SQLTIMESTAMP;
			
		}else if(trimedType.equals("FLOAT")){
			return DataType.FLOAT;
		}
		else if(trimedType.equals("DOUBLE")){
			return DataType.DOUBLE;
		}
		else if(trimedType.equals("BIT") 
				|| trimedType.equals("TINYINT")){
			return DataType.BOOL;
		}
		else if(trimedType.equals("BINARY") || trimedType.equals("VARBINARY") || trimedType.equals("LONGVARBINARY")){
			return DataType.BYTEARRAY;
		}
		else{
			return DataType.OBJECT;
		}
		
	}
	
	//////////////////////////////////////END//////////////////////////////////////
}
