package com.it.soul.lab.connect;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class JDBConnection implements Serializable{
	
	public static enum DriverClass{
		MYSQL,
		PostgraySQLv7,
		DB2,
		OracleOCI9i,
		SQLServer,
		H2_MEM,
		H2_FILE,
		H2_SERVER,
		H2_SERVER_TLS,
		NONE;
		
		public String toString(){
			String result = "";
			switch (this) {
			case MYSQL:
				result = "com.mysql.jdbc.Driver";
				break;
			case PostgraySQLv7:
				result = "org.postgresql.Driver";
				break;
			case DB2:
				result = "COM.ibm.db2.jdbc.app.DB2Driver";
				break;
			case OracleOCI9i:
				result = "oracle.jdbc.driver.OracleDriver";
				break;
			case SQLServer:
				result = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
				break;
			case H2_MEM:
			case H2_FILE:
			case H2_SERVER:
			case H2_SERVER_TLS:
				result = "org.h2.Driver";
				break;
			default:
				result = "sun.jdbc.odbc.JdbcOdbcDriver";
				break;
			}
			return result;
		}
		
		public String urlSchema() {
			String result = "";
			switch (this) {
			case MYSQL:
				result = "jdbc:mysql://";
				break;
			case PostgraySQLv7:
				result = "jdbc:postgresql://";
				break;
			case DB2:
				result = "jdbc:db2://";
				break;
			case OracleOCI9i:
				result = "jdbc:oracle:thin:@//";
				break;
			case SQLServer:
				result = "jdbc:sqlserver://";
				break;
			case H2_MEM:
				result = "jdbc:h2:mem:";
				break;
			case H2_FILE:
				result = "jdbc:h2:file:";
				break;
			case H2_SERVER:
				result = "jdbc:h2:tcp://";
				break;
			case H2_SERVER_TLS:
				result = "jdbc:h2:ssl://";
				break;
			default:
				result = "jdbc:odbc:";
				break;
			}
			return result;
		}
		
		public String defaultPort() {
			String result = "";
			switch (this) {
			case MYSQL:
				result = "3306";
				break;
			case PostgraySQLv7:
				result = "5432";
				break;
			case DB2:
				result = "446";
				break;
			case OracleOCI9i:
				result = "";
				break;
			case SQLServer:
				result = "1433";
				break;
			case H2_MEM:
			case H2_FILE:
				result = "";
				break;
			case H2_SERVER:
				result = "8084";
				break;
			case H2_SERVER_TLS:
				result = "8085";
				break;
			default:
				result = "";
				break;
			}
			return result;
		}
		
		public static DriverClass getMatchedDriver(String connectionURL) {
			DriverClass result = NONE;
			List<DriverClass> all = new ArrayList<>(EnumSet.allOf(DriverClass.class));
			for (DriverClass driverClass : all) {
				if (connectionURL.startsWith(driverClass.urlSchema())) {
					result = driverClass;
					break;
				}
			}
			return result;
		}
		
	}
	
	public static interface JDBConnectionBuilder{
		public JDBConnectionBuilder database(String name);
		public JDBConnectionBuilder credential(String name, String password);
		public Connection build() throws SQLException, Exception;
	}
	
	public static class Builder implements JDBConnectionBuilder{
		private JDBConnection dbConnection;
		private StringBuffer hostStr = new StringBuffer();
		private String dbName;
		private DriverClass driver;
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
		public Connection build() throws SQLException, Exception{
			if(dbConnection.serverUrl == null) {
				String hostName = hostStr.toString();
				if(hostName.isEmpty()) {
					generateHostName(null, null);
				}
				if(dbName == null || dbName.isEmpty()) {
					throw new SQLException("Database Name is empty.");
				}
				dbConnection.serverUrl = driver.urlSchema() + hostStr.toString() + dbName;
			}
			if(dbConnection.user == null || dbConnection.user.isEmpty()) {
				throw new SQLException("Username is missing.");
			}
			return dbConnection.getConnection();
		}
	}

	private static final long serialVersionUID = -6801905544609003454L;
	private String driver = null;
	private String serverUrl = null;
	private String user = null;
	private String password = null;
	
	private JDBConnection(){}

	private void printMetaInfos(DatabaseMetaData dma) throws Exception{
		//checkForWarning(conn.getWarnings());
		System.out.println("\nConnected To "+dma.getURL());
		System.out.println("Driver  "+dma.getDriverName());
		System.out.println("driver Version  "+dma.getDriverVersion());
	}

	private Connection getConnection() throws SQLException, Exception{
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
	 * @param Connection conn
	 * @throws SQLException
	 */
	public static void close(Connection conn) throws SQLException{
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
	        }
		}
	}
	
}
