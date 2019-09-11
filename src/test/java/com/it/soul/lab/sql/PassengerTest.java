package com.it.soul.lab.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.it.soul.lab.connect.JDBConnection;
import com.it.soul.lab.connect.JDBConnection.DriverClass;
import com.it.soul.lab.sql.query.models.Property;

public class PassengerTest {
	
	SQLExecutor exe;
	String[] names = new String[]{"Sohana","Towhid","Tanvir","Sumaiya","Tusin"};
	Integer[] ages = new Integer[] {15, 18, 28, 26, 32, 34, 25, 67};
	String password = "root";

	@Before @SuppressWarnings("Duplicates")
	public void before(){
		
		try {
			Connection conn = new JDBConnection.Builder(DriverClass.MYSQL)
					.database("testDB")
					.credential("root",password)
					.build();
			exe = new SQLExecutor(conn);
		} catch (SQLException e) {
			exe.close();
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getRandomName() {
		Random rand = new Random();
		int index = rand.nextInt(names.length);
		return names[index];
	}
	
	private Integer getRandomAge() {
		Random rand = new Random();
		int index = rand.nextInt(ages.length);
		return ages[index];
	}
	
	@After
	public void after(){
		exe.close();
	}
	
	@Test
	public void testUpdate() {
		Passenger passenger = new Passenger();
		passenger.setName(getRandomName());
		passenger.setAge(getRandomAge());
		try {
			Boolean res = passenger.insert(exe);
			Assert.assertTrue("Inserted", res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			passenger.setAge(getRandomAge());
			Boolean res = passenger.update(exe, "age");
			Assert.assertTrue("Updated", res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDelete() {
		Passenger passenger = new Passenger();
		passenger.setName(getRandomName());
		try {
			Boolean res = passenger.insert(exe);
			Assert.assertTrue("Inserted", res);
			Boolean del = passenger.delete(exe);
			Assert.assertTrue("Deleted", del);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test public void getPropertyTest() {
		Passenger passenger = new Passenger();
		
		//UseCase when id is @PrimaryKey and autoIncrement is true.
		Property prop = passenger.getPropertyTest("id", exe, true);
		Assert.assertTrue(prop == null);
		
		prop = passenger.getPropertyTest("id", exe, false);
		Assert.assertTrue(prop != null);
	}

	//@Test
	public void transactionTest(){
		//
        int rand = (new Random()).nextInt(2);
        boolean shouldFailed = rand >= 1;
		//
        try {
            exe.begin();
            System.out.println("begin()");
            Passenger passenger = new Passenger();
            passenger.setName(getRandomName());
            System.out.println("Name: " + passenger.getName());
            passenger.setAge(getRandomAge());
            passenger.insert(exe);
            try{
                Thread.sleep(5000);
            }catch (Exception e) {
                System.out.println(e.getMessage());
            }
            if (shouldFailed) {
                throw new SQLException("Going To Failed!!!");
            }
            exe.end();
            System.out.println("end()");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            try {
                exe.abort();
                System.out.println("abort()");
            } catch (SQLException e1) {System.out.println(e1.getMessage());}
        } finally {
            //
        }
    }

}
