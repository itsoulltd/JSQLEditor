/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.it.soul.lab.connect;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class JDBConnectionPool implements Serializable{

	private static Logger LOG = Logger.getLogger(JDBConnectionPool.class.getSimpleName());
	private static final long serialVersionUID = 8229833245259862179L;
	private static Object _lock = new Object();
	private static JDBConnectionPool _sharedInstance = null;
	private static int activeConnectionCount = 0;

	private static InitialContext initCtx=null;
	private static SortedMap<String, DataSource> dataSourcePool = null;
	private static String _DEFAULT_KEY = null;

	private JDBConnectionPool() throws NamingException {
		initCtx = new InitialContext();
	}

	/**
	 * Example JNDILookUp String
	 * "java:comp/env/jdbc/MySQLDB"
	 *
	 * @param JNDILookUp
	 * @throws NamingException
	 * @throws IllegalArgumentException
	 */
	private JDBConnectionPool(String JNDILookUp) throws NamingException,IllegalArgumentException{
		this();
		createNewSource(JNDILookUp);
	}

	private static void createNewSource(String JNDILookUp) throws IllegalArgumentException{
		if(JNDILookUp != null && !JNDILookUp.trim().equals("")){
			try {
				DataSource source = (DataSource) initCtx.lookup(JNDILookUp);
				addDataSource(JNDILookUp, source);
			} catch (NamingException e) {
				LOG.log(Level.WARNING, e.getMessage(), e);
			}
		}else{
			throw new IllegalArgumentException("Jndi Look Up string must not null!!!");
		}
	}

	private static void addDataSource(String JNDILookUp, DataSource source){
		String lookUpName = JNDILookUp;
		//
		String[] arr = JNDILookUp.split("/");
		if (arr.length > 0) {
			lookUpName = arr[arr.length-1];
		}
		//
		if(_DEFAULT_KEY == null){
			_DEFAULT_KEY = lookUpName;
		}
		if(!getDataSourcePool().containsKey(lookUpName)){
			getDataSourcePool().put(lookUpName, source);
		}
	}

	private static JDBConnectionPool poolInstance() {
		synchronized (_lock) {
			if(_sharedInstance != null){
				return _sharedInstance;
			}
		}
		LOG.info("Please Call configureConnectionPool at least once.");
		return null;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return _sharedInstance;
	}

	/**
	 * https://www.baeldung.com/java-finalize#avoiding-finalizers
	 */
	/*@Override
	protected void finalize() throws Throwable {
		super.finalize();
		_sharedInstance = null;
	}*/

	@Override
	public String toString() {
		return "ConnectDatabaseJDBCSingleton : " + serialVersionUID;
	}

	public static int activeConnections(){
		return activeConnectionCount;
	}

	private static void increasePoolCount(){
		synchronized (_lock) {
			activeConnectionCount++;
		}
	}

	//increase decrease
	private static void decreasePoolCount(){
		synchronized (_lock) {
			activeConnectionCount--;
		}
	}

	private static SortedMap<String, DataSource> getDataSourcePool() {
		if(dataSourcePool == null){
			dataSourcePool = new TreeMap<String, DataSource>();
		}
		return dataSourcePool;
	}

	///////////////////////////////JDBL Connection Pooling/////////////////////////

	/**
	 * User must have to call this method first.
	 * @param JNDILookUp
	 * @return
	 * @throws Exception
	 */
	public static void configure(String JNDILookUp) {
		synchronized (_lock) {
			if(_sharedInstance == null){
				try{
					_sharedInstance = new JDBConnectionPool(JNDILookUp);
				}catch(Exception e){
                    LOG.log(Level.WARNING, e.getMessage(), e);
				}
			}else{
				if(JNDILookUp != null && !JNDILookUp.trim().equals("")){
					createNewSource(JNDILookUp);
				}
			}
		}
	}

	public static void configure(String key, DataSource source) {
		synchronized (_lock){
			if(_sharedInstance == null){
				try{
					_sharedInstance = new JDBConnectionPool();
				}catch(Exception e){
                    LOG.log(Level.WARNING, e.getMessage(), e);
				}
			}
			if (key != null && !key.isEmpty()){
				addDataSource(key, source);
			}
		}
	}

	private DataSource findSourceByName(String key){
		if(!getDataSourcePool().containsKey(key))
			return getDataSourcePool().get(_DEFAULT_KEY);
		else
			return getDataSourcePool().get(key);
	}

	/**
	 *
	 */
	public static synchronized Connection connection() throws SQLException{
		Connection con = null;
		try{
			con = JDBConnectionPool.poolInstance().findSourceByName(_DEFAULT_KEY).getConnection();
			JDBConnectionPool.increasePoolCount();
		}catch(SQLException sqe){
			throw sqe;
		}
		return con;
	}

	public static synchronized Connection connection(String key) throws SQLException{
		Connection con = null;
		try{
			con = JDBConnectionPool.poolInstance().findSourceByName(key).getConnection();
			JDBConnectionPool.increasePoolCount();
		}catch(SQLException sqe){
			throw sqe;
		}
		return con;
	}

	/**
	 *
	 * @param userName
	 * @return
	 * @throws SQLException
	 */
	public static synchronized Connection connection(String key, String userName , String password)
			throws SQLException{
		Connection con = null;
		try{
			con = JDBConnectionPool.poolInstance().findSourceByName(key).getConnection(userName,password);
			JDBConnectionPool.increasePoolCount();
		}catch(SQLException sqe){
			throw sqe;
		}
		return con;

	}

	/**
	 *
	 * @param conn
	 * @throws SQLException
	 */
	public static synchronized void close(Connection conn) {
		try{
			if(conn != null && ! conn.getAutoCommit()){
				conn.commit();
			}
		}catch(SQLException sqe){
			try {
				if(!conn.getAutoCommit())
					conn.rollback();
			} catch (SQLException e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
			}
			sqe.printStackTrace();
		}
		finally{
			try {
				if(conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
			}
			JDBConnectionPool.decreasePoolCount();
		}
	}

	////////////////////////////////////End Pooling////////////////////////////
}