package com.it.soul.lab.sql;

import java.sql.Connection;

import org.junit.Assert;
import org.junit.Test;

import com.it.soul.lab.connect.JDBConnection;
import com.it.soul.lab.connect.DriverClass;

public class JDBConnectorTest {
	
	@Test
	public void testDriverWellDetail() {
		Connection conn = null;
		try {
			//-host(name:"",port:"") is optional: for localhost, pass it as null,null;
			//means host name will be generated based on
			//jdbc:<datasource>://localhost:<default-port>/<database-name> for the DriverClass;
			//Replace with: DriverClass.MYSQL
			conn = new JDBConnection.Builder(DriverClass.H2_EMBEDDED)
					.host("localhost", "3306")
					.database("testDB")
					.credential("root","root@123")
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
			conn = new JDBConnection.Builder(DriverClass.H2_EMBEDDED)
					.database("testDB")
					.credential("root", "root@123")
					.build();

		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(conn != null);
		System.out.println("----------------------");
	}

	@Test
	public void testConnUrlH2() {
		Connection conn = null;
		try {
			conn = new JDBConnection.Builder("jdbc:h2:mem:testH2DB;DB_CLOSE_DELAY=-1")
					.credential("sa","")
					.build();

		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(conn != null);
		System.out.println("----------------------");
	}

	@Test
	public void testConnUrlH2A() {
		Connection conn = null;
		try {
			conn = new JDBConnection.Builder(DriverClass.H2_EMBEDDED)
					.database("testH2DB")
					.credential("sa","")
					.query("DB_CLOSE_DELAY=-1")
					.build();

		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(conn != null);
		System.out.println("----------------------");
	}

	//If there are a MYSQL Server Running into localhost:
	//@Test
	public void testConnUrl() {
		Connection conn = null;
		try {
			conn = new JDBConnection.Builder("jdbc:mysql://localhost:3306/testDB")
					.credential("root","root@123")
					.build();

		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(conn != null);
		System.out.println("----------------------");
	}

}
