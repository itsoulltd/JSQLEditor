package com.it.soul.lab.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.it.soul.lab.connect.io.ScriptRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.it.soul.lab.connect.JDBConnection;
import com.it.soul.lab.connect.DriverClass;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;
import com.it.soul.lab.sql.query.models.Operator;
import com.it.soul.lab.sql.query.models.Property;

public class PersonTest {
	
	SQLExecutor exe;
	String[] names = new String[]{"Sohana","Towhid","Tanvir","Sumaiya","Tusin"};
	Integer[] ages = new Integer[] {15, 18, 28, 26, 32, 34, 25, 67};
	String password = "root";
	
	@Before @SuppressWarnings("Duplicates")
	public void before(){
		
		try {
			exe = new SQLExecutor.Builder(DriverClass.H2_EMBEDDED)
					.database("testH2DB")
					.credential("sa", "").build();
			//
			ScriptRunner runner = new ScriptRunner();
			File file = new File("testDB.sql");
			String[] cmds = runner.commands(runner.createStream(file));
			for (String cmd:cmds) {
				try {
					exe.executeDDLQuery(cmd);
				} catch (SQLException throwables) {}
			}
			//
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
		Person person = new Person();
		person.setUuid_idx(UUID.randomUUID().toString());
		person.setName_test(getRandomName());
		try {
			Boolean res = person.insert(exe);
			Assert.assertTrue("Inserted", res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		person.setAge(getRandomAge());
		person.setActive(false);
		person.setSalary(200.00);
		person.setDob(new Date(Calendar.getInstance().getTimeInMillis()));
		try {
			Boolean res = person.update(exe, "age","active","salary","dob");
			Assert.assertTrue("Updated", res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testInsert() {
		Person person = new Person();
		person.setUuid_idx(UUID.randomUUID().toString());
		person.setName_test(getRandomName());
		person.setAge(getRandomAge());
		//setting null will set the default value from @Column()
		// or set true/false as desired value:
		person.setActive(null);
		//
		person.setSalary(89200.00);
		person.setDob(new Date(new java.util.Date().getTime()));
		//
		try {
			Boolean res = person.insert(exe);
			Assert.assertTrue("Inserted", res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDelete() {
		Person person = new Person();
		person.setUuid_idx(UUID.randomUUID().toString());
		person.setName_test(getRandomName());
		try {
			Boolean res = person.insert(exe);
			Assert.assertTrue("Inserted", res);
			Boolean del = person.delete(exe);
			Assert.assertTrue("Deleted", del);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//@Test
	public void testReadClassOfTSQLExecutorPropertyArray() {
		try {
			List<Person> sons = Person.read(Person.class, exe, new Property("name", "Sohana"));
			Assert.assertTrue("Count is there", sons.size() > 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//@Test
	public void testReadClassOfTSQLExecutorExpressionInterpreter() {
		try {
			ExpressionInterpreter exp = new Expression(new Property("name", "Sohana"), Operator.EQUAL);
			List<Person> sons = Person.read(Person.class, exe, exp);
			for (Person person : sons) {
				if(person.getDob() != null) {
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					System.out.println(formatter.format(person.getDob()));
				}
				if(person.getCreateTime() != null) {
					SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
					System.out.println(formatter.format(person.getCreateTime()));
				}
				
			}
			Assert.assertTrue("Count is there", sons.size() > 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void getPropertyTest() {
		Person person = new Person();
		
		//UseCase when uuid is @PrimaryKey and auto is false.
		Property prop = person.getPropertyTest("uuid_idx", exe, true);
		Assert.assertTrue(prop == null);
		
		//prop = person.getPropertyTest("uuid_idx", exe, false);
		//Assert.assertTrue(prop != null);
		
		//prop = person.getPropertyTest("", exe, true);
		//Assert.assertTrue(prop == null);
		
		Property nameProp = person.getPropertyTest("name_test", exe, false);
		Assert.assertTrue(nameProp.getKey().equalsIgnoreCase("name"));
		
		Property salaryProp = person.getPropertyTest("salary", exe, false);
		Assert.assertTrue(salaryProp.getKey().equalsIgnoreCase("salary"));
	}
}
