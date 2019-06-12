package com.it.soul.lab.sql;

import java.sql.Connection;

import org.junit.Assert;
import org.junit.Test;

import com.it.soul.lab.connect.JDBConnection;
import com.it.soul.lab.connect.JDBConnection.DriverClass;

public class JDBConnectorTest {

	@Test
	public void testConnUrl() {
		Connection conn = null;
		try {
			conn = new JDBConnection.Builder("jdbc:mysql://localhost:3306/testDB")
					.credential("root","****")
					.build();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(conn != null);
		System.out.println("----------------------");
	}
	
	@Test
	public void testDriver() {
		Connection conn = null;
		try {
			conn = new JDBConnection.Builder(DriverClass.MYSQL)
					.database("testDB")
					.credential("root","****")
					.build();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(conn != null);
		System.out.println("----------------------");
	}
	
	@Test
	public void testConnUrlDetail() {
		Connection conn = null;
		try {
			conn = new JDBConnection.Builder("jdbc:mysql://localhost:3306/testDB")
					.host("dasda", "asdasd") //Has no impact
					.database("asdasd") //Has no impact
					.credential("root","****")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(conn != null);
		System.out.println("----------------------");
	}
	
	@Test
	public void testDriverBadDetail() {
		Connection conn = null;
		try {
			conn = new JDBConnection.Builder(DriverClass.MYSQL)
					.host("fghjk", "asdasd") //Have to be provided in correct form
					.database("qwewe") //Have to be provided in correct form
					.credential("root","****")
					.build();
			
		} catch (Exception e) {
			System.out.println("SQLException:"+e.getMessage());
		}
		
		try {
			conn = new JDBConnection.Builder(DriverClass.MYSQL)
					.credential("root", "****")
					.database("asdasd").build();
		}catch(Exception e) {
			System.out.println("SQLException:"+e.getMessage());
		}

		try {
			conn = new JDBConnection.Builder(DriverClass.MYSQL)
					.database("fdaf").build();
		} catch (Exception e) {
			System.out.println("SQLException:"+e.getMessage());
		}

		try {
			conn = new JDBConnection.Builder(DriverClass.MYSQL)
					.credential("root", "****").build();
		} catch (Exception e) {
			System.out.println("SQLException:"+e.getMessage());
		}

		try {
			conn = new JDBConnection.Builder(DriverClass.MYSQL)
					.credential("root", "")
					.database("testDB").build();
		} catch (Exception e) {
			System.out.println("SQLException:"+e.getMessage());
		}

		try {
			conn = new JDBConnection.Builder(DriverClass.MYSQL)
					.credential(null, "****")
					.database("testDB").build();
		} catch (Exception e) {
			System.out.println("SQLException:"+e.getMessage());
		}
		
		
		Assert.assertTrue(conn == null);
		System.out.println("----------------------");
	}
	
	@Test
	public void testDriverWellDetail() {
		Connection conn = null;
		try {
			//-host(name:"",port:"") is optional: for localhost, pass it as null,null; 
			//means host name will be generated based on 
			//jdbc:<datasource>://localhost:<default-port>/<database-name> for the DriverClass;
			conn = new JDBConnection.Builder(DriverClass.MYSQL)
					.host("localhost", "3306")
					.database("testDB")
					.credential("root","****")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(conn != null);
		System.out.println("----------------------");
	}

}
