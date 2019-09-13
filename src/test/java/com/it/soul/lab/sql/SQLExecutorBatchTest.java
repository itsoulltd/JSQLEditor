package com.it.soul.lab.sql;

import com.it.soul.lab.connect.DriverClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SQLExecutorBatchTest {

    private SQLExecutor exe;
    String password = "root";

    @Before
    public void setUp() throws Exception {
        exe = new SQLExecutor.Builder(DriverClass.MYSQL)
                .database("testDB").credential("root", password).build();
    }

    @After
    public void tearDown() throws Exception {
        exe.close();
        exe = null;
    }

    @Test
    public void executeUpdate() {
    }

    @Test
    public void executeDelete() {
    }

    @Test
    public void executeInsert() {
    }
}