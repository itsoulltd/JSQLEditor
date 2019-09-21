package com.it.soul.lab.sql;

import com.it.soul.lab.connect.DriverClass;
import com.it.soul.lab.connect.JDBConnection;
import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.*;
import com.it.soul.lab.sql.query.models.DataType;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.Row;
import com.it.soul.lab.sql.query.models.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLExecutor extends AbstractExecutor implements QueryExecutor<SQLSelectQuery, SQLInsertQuery, SQLUpdateQuery, SQLDeleteQuery, SQLScalarQuery> {

	public static class Builder {
		private JDBConnection.Builder connectionBuilder;
		public Builder(DriverClass driver){
			connectionBuilder = new JDBConnection.Builder(driver);
		}
		public Builder host(String name, String port) {
			connectionBuilder.host(name, port);
			return this;
		}
		public Builder database(String name) {
			connectionBuilder.database(name);
			return this;
		}
		public Builder credential(String name, String password){
			connectionBuilder.credential(name, password);
			return this;
		}
		public SQLExecutor build() throws Exception {
			Connection conn = connectionBuilder.build();
			return new SQLExecutor(conn);
		}
	}

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
	 * 
	 * @param o
	 */
	public void displayCollection(Object o){
		System.out.println(toString(o));
	}

/////////////////////////////////////QueryExecutor-Interface///////////////


	@Override
	public SQLQuery.Builder createQueryBuilder(QueryType queryType) {
		return new SQLQuery.Builder(queryType);
	}

	@Override
	public <T extends Entity> List<T> executeCRUDQuery(String query, Class<T> type) throws SQLException, IllegalAccessException, InstantiationException {
		ResultSet set = executeCRUDQuery(query);
		if (set != null){
			Table table = collection(set);
			List results = table.inflate(type, Entity.mapColumnsToProperties(type));
			return results;
		}
		return null;
	}

	public Integer executeUpdate(SQLUpdateQuery query) throws SQLException{

		Row setProperties = query.getRow();
		if(setProperties == null
				|| setProperties.size() <= 0){
			throw new SQLException("Set Parameter Should not be bull or empty!!!");
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

    public Integer executeUpdate(String query) throws SQLException{
        if(query == null
                || query.isEmpty()){
            throw new SQLException("Query Should not be bull or empty!!!");
        }
        PreparedStatement stmt=null;
        String queryStr = query;
        return getRowUpdated( stmt, queryStr);
    }

    private int getRowUpdated(PreparedStatement stmt, String queryStr) throws SQLException {
        int rowUpdated = 0;
        try{
            if(conn != null){
                stmt = conn.prepareStatement(queryStr);
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

    @Override
    public Integer[] executeUpdate(int size, SQLUpdateQuery updateQuery, List<Row> rows) throws SQLException, IllegalArgumentException {

        if(rows == null
                || rows.size() <= 0){
            throw new SQLException("Set Parameter Should not be bull or empty!!!");
        }

        List<Integer> affectedRows = new ArrayList<Integer>();
        PreparedStatement stmt = null;
        String query = updateQuery.toString();

        boolean notBegin = conn.getAutoCommit();
        try{
            size = (size < 100) ? 100 : size;//Least should be 100
            if(conn != null){
                //
                List<int[]> batchUpdatedRowsCount = new ArrayList<int[]>();
                if(notBegin) begin();
                stmt = conn.prepareStatement(query);
                int batchCount = 1;
                for (int index = 0; index < rows.size(); index++) {

                    String[] keySet = rows.get(index).getKeys();
                    Map<String, Property> row = rows.get(index).keyValueMap();
                    stmt = bindValueToStatement(stmt, 1, keySet, row);

                    int length = keySet.length;
                    //Row whereProperties = getLeastAppropriateProperties(whereClause, index);
                    Row whereProperties = updateQuery.getWhereProperties();
                    String[] whereKeySet = whereProperties.getKeys();
                    Map<String, Property> whereRow = whereProperties.keyValueMap();
                    stmt = bindValueToStatement(stmt, length + 1, whereKeySet, whereRow);

                    stmt.addBatch();
                    if ((++batchCount % size) == 0) {
                        batchUpdatedRowsCount.add(stmt.executeBatch());
                    }
                }
                if(rows.size() % size != 0)
                    batchUpdatedRowsCount.add(stmt.executeBatch());
                //
                if(notBegin) end();
                //
                for (int[] rr  : batchUpdatedRowsCount) {
                    for(int i = 0; i < rr.length ; i++){
                        affectedRows.add(rr[i]);
                    }
                }

            }
        }catch(SQLException | IllegalArgumentException exp){
            if(notBegin) abort();
            throw exp;
        }finally{
            clearBatch(stmt);
        }
        return affectedRows.toArray(new Integer[]{});
    }

    private String bindValueToQuery(SQLQuery query){
	    return query.bindValueToString();
    }

    @Override
    public Integer[] executeUpdate(int size, List<SQLUpdateQuery> queries) throws SQLException, IllegalArgumentException {
        if(queries == null
                || queries.size() <= 0){
            throw new SQLException("Set Parameter Should not be bull or empty!!!");
        }

        List<Integer> affectedRows = new ArrayList<Integer>();
        Statement stmt = null;
        boolean notBegin = conn.getAutoCommit();
        try{
            size = (size < 100) ? 100 : size;//Least should be 100
            if(conn != null){
                //
                List<int[]> batchUpdatedRowsCount = new ArrayList<int[]>();
                if(notBegin) begin();
                stmt = conn.createStatement();
                int batchCount = 1;
                for (int index = 0; index < queries.size(); index++) {
                    SQLUpdateQuery upQuery = queries.get(index);
                    String queryAfter = bindValueToQuery(upQuery);
                    stmt.addBatch(queryAfter);
                    if ((++batchCount % size) == 0) {
                        batchUpdatedRowsCount.add(stmt.executeBatch());
                    }
                }
                if(queries.size() % size != 0)
                    batchUpdatedRowsCount.add(stmt.executeBatch());
                //
                if(notBegin) end();
                //
                for (int[] rr  : batchUpdatedRowsCount) {
                    for(int i = 0; i < rr.length ; i++){
                        affectedRows.add(rr[i]);
                    }
                }

            }
        }catch(SQLException | IllegalArgumentException exp){
            if(notBegin) abort();
            throw exp;
        }finally{
            clearBatch(stmt);
        }
        return affectedRows.toArray(new Integer[]{});
    }

    public Integer executeDelete(SQLDeleteQuery deleteQuery)
			throws SQLException{

		if(deleteQuery.getWhereParamExpressions() == null || deleteQuery.getWhereParamExpressions().size() <= 0){
			throw new SQLException("Where parameter should not be null or empty!!!");
		}

		int rowUpdated = 0;
		PreparedStatement stmt=null;
		String query = deleteQuery.toString();
		try{
			if(conn != null){
				stmt = conn.prepareStatement(query);
				stmt = bindValueToStatement(stmt, 1, deleteQuery.getWhereParams(), deleteQuery.getWhereProperties().keyValueMap());
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

    public Integer executeDelete(String deleteQuery)
            throws SQLException{
        if(deleteQuery == null || deleteQuery.isEmpty()){
            throw new SQLException("Query should not be null or empty!!!");
        }
        PreparedStatement stmt=null;
        String query = deleteQuery;
        int rowUpdated = getRowUpdated(stmt, query);
        return rowUpdated;
    }

    @Override
    public Integer executeDelete(int size, SQLDeleteQuery deleteQuery, List<Row> where) throws SQLException {
	    //
        if(deleteQuery.getWhereParams() == null || deleteQuery.getWhereParams().length <= 0){
            throw new SQLException("Where parameter should not be null or empty!!!");
        }
        //
        int rowUpdated = 0;
        PreparedStatement stmt=null;
        String query = deleteQuery.toString();
        String[] whereKeySet = where.get(0).getKeys();
        boolean notBegin = conn.getAutoCommit();
        try{
            size = (size < 100) ? 100 : size;//Least should be 100
            if(conn != null){
                if(notBegin) begin();
                int batchCount = 1;
                stmt = conn.prepareStatement(query);
                for (Row paramValue: where) {
                    stmt = bindValueToStatement(stmt, 1, whereKeySet, paramValue.keyValueMap());
                    stmt.addBatch();
                    if ((++batchCount % size) == 0) {
                        stmt.executeBatch();
                    }
                }
                if(where.size() % size != 0)
                    stmt.executeBatch();
                //
                if(notBegin) end();
            }
        }catch(SQLException | IllegalArgumentException exp){
            if(notBegin) abort();
            throw exp;
        }finally{
            clearBatch(stmt);
        }
        return rowUpdated;
    }

	public Integer executeInsert(boolean autoId, String query)
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
				if (autoId) {
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

	public Integer executeInsert(boolean autoId, SQLInsertQuery insertQuery)
			throws SQLException, IllegalArgumentException{

		if(insertQuery.getColumns() == null || insertQuery.getColumns().length <= 0){
			throw new SQLException("Parameter should not be null or empty!!!");
		}

		int affectedRows = 0;
		PreparedStatement stmt=null;
		String query = insertQuery.toString();

		try{
			if(conn != null){
				if(autoId){
					stmt = conn.prepareStatement(query,	Statement.RETURN_GENERATED_KEYS);
					stmt = bindValueToStatement(stmt, 1,insertQuery.getRow().getKeys(), insertQuery.getRow().keyValueMap());
					stmt.executeUpdate();
					ResultSet set = stmt.getGeneratedKeys();
					if(set != null && set.next()){
						affectedRows = set.getInt(1);
					}
				}else{
					stmt = conn.prepareStatement(query);
					stmt = bindValueToStatement(stmt, 1, insertQuery.getRow().getKeys(), insertQuery.getRow().keyValueMap());
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

    @Override
    public Integer[] executeInsert(boolean autoId, int size, SQLInsertQuery insertQuery, List<Row> rows) throws SQLException, IllegalArgumentException {
        if(rows == null || rows.size() <= 0){
            throw new SQLException("Parameter should not be null or empty!!!");
        }
        List<Integer> affectedRows = new ArrayList<Integer>();
        PreparedStatement stmt=null;
        //
        Object[] keySet = rows.get(0).getKeys();
        String query = insertQuery.toString();
        boolean notBegin = conn.getAutoCommit();
        //
        try{
            size = (size < 100) ? 100 : size;//Least should be 100
            if(conn != null){
                if(notBegin) begin();
                stmt = autoId
                        ? conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)
                        : conn.prepareStatement(query);
                int batchCount = 1;
                List<int[]> batchUpdatedRowsCount = new ArrayList<int[]>();
                for (Row row : rows) {
                    stmt = bindValueToStatement(stmt, 1, keySet, row.keyValueMap());
                    stmt.addBatch();
                    if((++batchCount % size) == 0){
                        batchUpdatedRowsCount.add(stmt.executeBatch());
                    }
                }
                if(rows.size() % size != 0)
                    batchUpdatedRowsCount.add(stmt.executeBatch());
                //
                if(notBegin) end();
                for (int[] rr  : batchUpdatedRowsCount) {
                    for(int i = 0; i < rr.length ; i++){
                        affectedRows.add(rr[i]);
                    }
                }
                //
            }
        }catch(SQLException | IllegalArgumentException exp){
            if(notBegin) abort();
            throw exp;
        }finally{
            clearBatch(stmt);
        }
        return affectedRows.toArray(new Integer[]{});
    }

    private void clearBatch(Statement stmt) throws SQLException {
        if(stmt != null){
            try {
                stmt.clearBatch();
            } catch (Exception e) {
                e.printStackTrace();
            }
            stmt.close();
        }
    }

	public Integer getScalerValue(String query)
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

	public Integer getScalarValue(SQLScalarQuery scalerQuery)
			throws SQLException{

		ResultSet rs = null;
		PreparedStatement pstmt = null;
		int rowCount = 0;
		String query = scalerQuery.toString();
		Row whereClause = scalerQuery.getWhereProperties();
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

	public <T extends Entity> List<T> executeSelect(String query, Class<T> type) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		return executeSelect(query, type, Entity.mapColumnsToProperties(type));
	}

	@Override
	public <T> List<T> executeSelect(String query, Class<T> type, Map<String, String> mappingKeys) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		ResultSet set = executeSelect(query);
		Table table = collection(set);
		List result = table.inflate(type, mappingKeys);
		return result;
	}

	public <T> List<T> executeSelect(SQLSelectQuery query, Class<T> type) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		return executeSelect(query, type, null);
	}

	@Override
	public <T> List<T> executeSelect(SQLSelectQuery query, Class<T> type, Map<String, String> mappingKeys) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		ResultSet set = executeSelect(query);
		Table table = collection(set);
		List result = table.inflate(type, mappingKeys);
		return result;
	}

	public Boolean executeDDLQuery(String query) throws SQLException{

		if(query == null
				|| query.length() <=0
				/*|| !query.trim().toLowerCase().startsWith("create")
				|| !query.trim().toLowerCase().startsWith("drop")
				|| !query.trim().toLowerCase().startsWith("alter")*/){
			throw new SQLException("Bad Formatted Query : " + query);
		}

		return executeDDLStatement(query);
	}

	protected Boolean executeDDLStatement(String query) throws SQLException {
		boolean isCreated = false;
		PreparedStatement stmt = null;
		try{
			if(conn != null){
				stmt = conn.prepareStatement(query);
				int result = stmt.executeUpdate();
				isCreated = result == 0;
			}
		}catch(SQLException exp){
			throw exp;
		}finally{
			if(stmt != null) stmt.close();
		}
		return isCreated;
	}

////////////////////////////////////Block Of Queries///////////////////////

	public boolean useDatabase(String database) throws SQLException {

		if (database == null || database.trim().isEmpty()) return false;

		String query = "USE " + database;
		return executeDDLStatement(query);
	}

	public <T extends Entity> Boolean createTable(Class<T> tableType, DriverClass driverClass) throws SQLException {
		String tableNameStr = getTableName(tableType);
		if (tableNameStr == null) return false;

		StringBuffer headBuffer = new StringBuffer("CREATE TABLE IF NOT EXISTS " + tableNameStr);

		if (driverClass == DriverClass.MYSQL){
			//TODO:
		}else{
			//TODO:
		}
		return false;
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
     * Query for Update,Insert,Delete
     * @param query
     * @return Number Of affected rows
     */
    protected ResultSet executeCRUDQuery(String query) throws SQLException{
    	
    	if(query == null 
				|| query.length() <=0){
			throw new SQLException("Bad Formated Query : " + query);
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

    @Deprecated
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
     * Query for select
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
		Table result = createTableFrom(rst, columns);
    	return result;
    }

	protected Table createTableFrom(ResultSet rst, String[] columns) {
		Table result = new Table();
		result.setRows(createRowsFrom(rst, columns));
		return result;
	}

	/**
	 * 
	 * @param rst
	 * @return
	 */
    public Table collection(ResultSet rst){
		Table result = createTableFrom(rst);
		return result;
	}

	protected Table createTableFrom(ResultSet rst) {
		Table result = new Table();
		result.setRows(createRowsFrom(rst));
		return result;
	}

	public List<Row> convertToLists(ResultSet rst){
		List<Row> result = createRowsFrom(rst);
		return result;
	}

	protected List<Row> createRowsFrom(ResultSet rst) {
		List<Row> result = new ArrayList<Row>();
		try{
			//IF cursor is moved till last row. Then set to the above first row.
			if(rst.getType() == ResultSet.TYPE_SCROLL_SENSITIVE && rst.isAfterLast()){
                rst.beforeFirst();
            }
			ResultSetMetaData rsmd = rst.getMetaData();
			int numCol = rsmd.getColumnCount();

			while(rst.next()){ //For each Row
				Row row = createRowFrom(rst, rsmd, numCol);
				if(row.size() > 0)
					result.add(row);
			}
		}catch(SQLException exp){
			result = null;
			exp.getStackTrace();
		}
		return result;
	}

	protected List<Row> createRowsFrom(ResultSet rst, String...columns) {

    	if (columns == null || columns.length == 0) return createRowsFrom(rst);

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
				for(int x : columnIndecies){ //For each column in the columns
					Property property = createPropertyFrom(rst, rsmd, x);
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

	protected Row createRowFrom(ResultSet rst, ResultSetMetaData rsmd, int numCol) throws SQLException {
		Row row = new Row();
		for(int x = 1; x <= numCol; x++){ //For each column in a Row
			Property property = createPropertyFrom(rst, rsmd, x);
			row.add(property);
		}
		return row;
	}

	protected Property createPropertyFrom(ResultSet rst, ResultSetMetaData rsmd, int x) throws SQLException {
		String key = rsmd.getColumnName(x);
		DataType type = convertDataType(rsmd.getColumnTypeName(x));
		Object value = getValueFromResultSet(type, rst, x);
		return new Property(key, value);
	}

	public List<Row> convertToLists(ResultSet rst, String...columns){
		if(columns.length == 0){
            return convertToLists(rst);
        }
		List<Row> result = createRowsFrom(rst, columns);
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
					DataType type = convertDataType(rsmd.getColumnTypeName(x));
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
                    Property property = createPropertyFrom(rst, rsmd, x);
                    row.add(property);
                }
                result = row;
            }else{
                if(!rst.isAfterLast()){
                    while(rst.next()){
                        if(rowIndex == rst.getRow()){
                            Row row = new Row();
                            for(int x = 1; x <= numCol; x++){ //For each column in a Row
								Property property = createPropertyFrom(rst, rsmd, x);
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
				Property prop = new Property(key,value);
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
				Property prop = new Property(key, value);
				result.add(prop);
			}
		}catch(SQLException exp){
			result = null;
			exp.getStackTrace();
		}
		return result;
	}
	
	public Object createBlob(String val) throws SQLException {
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
            			if (property.getType() == DataType.NULL_SKIP)
            			    continue;

            			switch (property.getType()) {
	            			case STRING:
	            				stmt.setString(index++, (property.getValue() != null)
                                        ? property.getValue().toString().trim()
                                        : null);
	            				break;
	            			case INT:
	            				if(property.getValue() != null){
	            					stmt.setInt(index++, (Integer)property.getValue());
	            				}else{
	            					stmt.setNull(index++, java.sql.Types.INTEGER);
	            				}
	            				break;
                            case LONG:
                                if(property.getValue() != null){
                                    stmt.setLong(index++, (Long)property.getValue());
                                }else{
                                    stmt.setNull(index++, Types.BIGINT);
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
                            case LIST:
                                if (property.getValue() != null){
                                    List items = (List) property.getValue();
                                    Object obj = (items.size() > 0) ? items.get(0) : "";
                                    if (obj instanceof Integer){
                                        stmt.setArray(index++, conn.createArrayOf("integer", items.toArray(new Integer[0])));
                                    }
                                    else if (obj instanceof Double){
                                        stmt.setArray(index++, conn.createArrayOf("double", items.toArray(new Double[0])));
                                    }
                                    else if (obj instanceof Float){
                                        stmt.setArray(index++, conn.createArrayOf("float", items.toArray(new Float[0])));
                                    }
                                    else if (obj instanceof Long){
                                        stmt.setArray(index++, conn.createArrayOf("long", items.toArray(new Long[0])));
                                    }
                                    else if((obj instanceof Timestamp)
                                            || (obj instanceof Time)
                                            || obj instanceof Date){
                                        stmt.setArray(index++, conn.createArrayOf("timestamp", items.toArray()));
                                    }
                                    else if(obj instanceof String) {
                                        stmt.setArray(index++, conn.createArrayOf("string", items.toArray(new String[0])));
                                    }
                                    else {
                                        stmt.setArray(index++, conn.createArrayOf("object", items.toArray()));
                                    }
                                }else{
                                    stmt.setNull(index++, Types.ARRAY);
                                }
                                break;
	            			default:
	            				if(property.getValue() != null)
	            				    stmt.setObject(index++, property.getValue());
	            				else
	            				    stmt.setNull(index++, Types.NULL);
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
	
	protected Object getValueFromResultSet(DataType type, ResultSet rst, int index)
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

	//////////////////////////////////////END//////////////////////////////////////
}
