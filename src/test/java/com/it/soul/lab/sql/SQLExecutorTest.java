package com.it.soul.lab.sql;

import com.it.soul.lab.connect.JDBConnection;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class SQLExecutorTest {

    private SQLExecutor exe;
    String password = "root";

    @Before
    public void setUp() throws Exception {
        exe = new SQLExecutor.Builder(JDBConnection.DriverClass.MYSQL)
                .database("testDB").credential("root", password).build();
    }

    @After
    public void tearDown() throws Exception {
        exe.close();
        exe = null;
    }

    @Test
    public void useTest(){
        try {
            //exe.useDatabase("testDB");
            boolean isCreated = exe.executeDDLQuery("CREATE TABLE IF NOT EXISTS Passenger (" +
                    "id int auto_increment primary key" +
                    ", name varchar(1024) null" +
                    ", age  int default '18' null" +
                    ", sex varchar(12) null" +
                    ", constraint Passenger_id_uindex unique (id));");
            Assert.assertTrue("Created Successfully", isCreated);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}