package com.it.soul.lab.connect;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBConnection implements Serializable{

	public static class Builder implements JDBConnectionBuilder{
		private JDBConnection dbConnection;
		private StringBuffer hostStr = new StringBuffer();
		private String dbName;
		private DriverClass driver;
		private String linkQuery;
		public Builder(String connectionURL){
			dbConnection = new JDBConnection();
			dbConnection.serverUrl = connectionURL;
			driver = DriverClass.getMatchedDriver(connectionURL);
			dbConnection.driver = driver.toString();
		}
		public Builder(DriverClass driver){
			dbConnection = new JDBConnection();
			dbConnection.driver = driver.toString();
			this.driver = driver;
		}
		public Builder host(String name, String port) {
			generateHostName(name, port);
			return this;
		}
		private void generateHostName(String name, String port) {
			if((name == null || name.isEmpty())) {
				hostStr.append("localhost");
			}else {
				hostStr.append(name); 
			}
			if ((port == null || port.isEmpty())) {
				hostStr.append(":"+driver.defaultPort());
			}else {
				hostStr.append(":"+port);
			}
		}
		public JDBConnectionBuilder database(String name) {
			if(name == null || name.isEmpty()) {dbName = "/";}
			else {dbName = "/"+name;}
			return this;
		}
		public JDBConnectionBuilder credential(String name, String password){
			dbConnection.user = name;
			dbConnection.password = password;
			return this;
		}
		public JDBConnectionBuilder query(String query) {
		    if (linkQuery != null && !linkQuery.isEmpty()) return this;
		    if (query != null && !query.trim().startsWith("?")) query = "?" + query.trim();
		    linkQuery = query;
			return this;
		}
		public Connection build() throws SQLException{
			if(dbConnection.serverUrl == null) {
				String hostName = hostStr.toString();
				if(hostName.isEmpty()) {
					generateHostName(null, null);
				}
				if(dbName == null || dbName.isEmpty()) {
					throw new SQLException("Database Name is empty.");
				}
                if (linkQuery != null && !linkQuery.isEmpty())
                    dbConnection.serverUrl = driver.urlSchema() + hostStr.toString() + dbName + linkQuery;
				else
				    dbConnection.serverUrl = driver.urlSchema() + hostStr.toString() + dbName;
			}
			if(dbConnection.user == null || dbConnection.user.isEmpty()) {
				throw new SQLException("Username is missing.");
			}
			try {
				return dbConnection.getConnection();
			} catch (Exception e) {
				throw new SQLException(e.getMessage());
			}
		}
	}

	private static Logger LOG = Logger.getLogger(JDBConnection.class.getSimpleName());
	private static final long serialVersionUID = -6801905544609003454L;
	private String driver = null;
	private String serverUrl = null;
	private String user = null;
	private String password = null;
	
	private JDBConnection(){}

	private void printMetaInfos(DatabaseMetaData dma) throws Exception{
		//checkForWarning(conn.getWarnings());
		LOG.info("\nConnected To "+dma.getURL());
		LOG.info("Driver  "+dma.getDriverName());
		LOG.info("driver Version  "+dma.getDriverVersion());
	}

	private Connection getConnection() throws Exception{
		Connection conn = null;
		try{
			if(getDriver() != null 
					&& getServerUrl() != null
					&& getUser() != null ){
				Class.forName(getDriver());
				setPassword((getPassword() != null) ? getPassword() : "");
				conn = DriverManager.getConnection(getServerUrl(),getUser(),getPassword());
				printMetaInfos(conn.getMetaData());
			}else{
				throw new IllegalArgumentException("Database Engine driver OR Server URL OR UserName should not be empty.");
			}
		}catch(SQLException exp){
			throw exp;
		}catch(Exception e){
			throw e;
		}
		return conn;
	}

	private String getDriver() {
		return driver;
	}
	private String getServerUrl() {
		return serverUrl;
	}
	private String getUser() {
		return user;
	}
	private String getPassword() {
		return password;
	}
	private void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param connection
	 * @throws SQLException
	 */
	public static void close(Connection connection) throws SQLException{
		if(connection != null && !connection.isClosed()){
			try{
				if(!connection.getAutoCommit())
					connection.commit();
			}catch(SQLException exp){
				if(!connection.getAutoCommit())
					connection.rollback();
				throw exp;
			}
			finally{
	        	try {
					if(connection != null && !connection.isClosed())
						connection.close();
				} catch (SQLException e) {
                    LOG.log(Level.WARNING, e.getMessage(), e);
				}
	        }
		}
	}
	
}
