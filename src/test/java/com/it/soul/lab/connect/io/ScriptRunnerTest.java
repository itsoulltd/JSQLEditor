package com.it.soul.lab.connect.io;

import com.it.soul.lab.connect.DriverClass;
import com.it.soul.lab.connect.JDBConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;

import static org.junit.Assert.*;

public class ScriptRunnerTest {

    Connection connection;
    ScriptRunner runner;

    @Before
    public void setUp() throws Exception {
        connection = new JDBConnection.Builder(DriverClass.H2_EMBEDDED)
                .database("testH2DB")
                .credential("sa", "").build();
        runner = new ScriptRunner();
    }

    @After
    public void tearDown() throws Exception {
        runner = null;
        if (!connection.isClosed())
            connection.close();
    }

    @Test
    public void runScripts(){
        File file = new File("src/test/resources/testDB.sql");
        if (file.isFile()){
            System.out.println(file.getAbsolutePath());
            try {
                String[] cmds = runner.commands(new FileInputStream(file));
                runner.execute(cmds, connection);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void runScriptsV2(){
        File file = new File("testDB.sql");
        String[] cmds = runner.commands(runner.createStream(file));
        runner.execute(cmds, connection);
    }
}