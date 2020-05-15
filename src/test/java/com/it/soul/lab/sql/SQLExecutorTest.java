package com.it.soul.lab.sql;

import com.it.soul.lab.connect.DriverClass;
import com.it.soul.lab.connect.io.ScriptRunner;
import com.it.soul.lab.sql.query.*;
import com.it.soul.lab.sql.query.models.DataType;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.Row;
import com.it.soul.lab.sql.query.models.Where;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.SQLException;

public class SQLExecutorTest {

    private SQLExecutor exe;
    String password = "root";

    @Before
    public void setUp() throws Exception {
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

    @Test
    public void insert() throws SQLException {
        SQLInsertQuery query = new SQLQuery.Builder(QueryType.INSERT)
                .into("Passenger")
                .values(new Row()
                        .add("name","MyName-A")
                        .add(new Property("age", null, DataType.INT))
                        .add("sex", null).getProperties().toArray(new Property[0]))
                .build();
        int res = exe.executeInsert(true, query);
        Assert.assertTrue(res > 0);
    }

    @Test
    public void update() throws SQLException {
        SQLUpdateQuery query = new SQLQuery.Builder(QueryType.UPDATE)
                .set(new Row().add("age",19).getProperties().toArray(new Property[0]))
                .from("Passenger")
                .where(new Where("sex").isNull())
                .build();
        int res = exe.executeUpdate(query);
        Assert.assertTrue(res >= 0);
    }

    @Test
    public void delete() throws SQLException {
        SQLDeleteQuery query = new SQLQuery.Builder(QueryType.DELETE)
                .rowsFrom("Passenger")
                .where(new Where("sex").isNull())
                .build();
        int deleted = exe.executeDelete(query);
        Assert.assertTrue(deleted >= 0);
    }
}