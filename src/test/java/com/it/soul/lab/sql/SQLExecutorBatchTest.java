package com.it.soul.lab.sql;

import com.it.soul.lab.connect.DriverClass;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLQuery;
import com.it.soul.lab.sql.query.SQLUpdateQuery;
import com.it.soul.lab.sql.query.models.DataType;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.Where;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

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
    public void updateQueryValueBinding(){
        SQLUpdateQuery updateQuery = new SQLQuery.Builder(QueryType.UPDATE)
                .set(new Property("name", "Towhid"), new Property("age", 36), new Property("sex", "male"))
                .from("Passenger")
                .where(new Where("uuid").notNull())
                .build();
        String buffer = exe.updateQueryValueBinding(updateQuery);
        System.out.println(buffer);
        //
        updateQuery = new SQLQuery.Builder(QueryType.UPDATE)
                .set(new Property("name", "Towhid"), new Property("age", 36), new Property("sex", "male"))
                .from("Passenger")
                .where(new Where("uuid").isEqualTo(UUID.randomUUID().toString()))
                .build();
        buffer = exe.updateQueryValueBinding(updateQuery);
        System.out.println(buffer);
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