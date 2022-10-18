package com.it.soul.lab.sql;

import com.it.soul.lab.PerformanceLogger;
import com.it.soul.lab.connect.DriverClass;
import com.it.soul.lab.connect.io.ScriptRunner;
import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLQuery;
import com.it.soul.lab.sql.query.SQLScalarQuery;
import com.it.soul.lab.sql.query.models.Predicate;
import com.it.soul.lab.sql.query.models.Row;
import com.it.soul.lab.sql.query.models.Where;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EntityCRUDTest {

    private SQLExecutor exe;

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
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        //
    }

    @After
    public void tearDown() throws Exception {
        exe.close();
        exe = null;
    }

    public enum Gender {
        MALE,
        FEMALE,
        TRANSGENDER,
        NONE
    }

    private void insertSeedPassenger(SQLExecutor executor) {
        try {
            new Passenger("Towhid Islam", Gender.MALE.name(), 37).insert(executor);
            new Passenger("Sohana Islam Khan", Gender.FEMALE.name(), 27).insert(executor);
            new Passenger("Tanvir Islam", Gender.MALE.name(), 33).insert(executor);
            new Passenger("Sumaiya Islam", Gender.FEMALE.name(), 24).insert(executor);
            new Passenger("Tasnim Islam Khan", Gender.FEMALE.name(), 19).insert(executor);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void insertSeedPassengerInBatch(SQLExecutor executor) {
        try {
            List<Row> batch = new ArrayList<>();
            batch.add(new Passenger("Towhid Islam", Gender.MALE.name(), 37).getRow());
            batch.add(new Passenger("Sohana Islam ", Gender.FEMALE.name(), 27).getRow());
            batch.add(new Passenger("Tanvir Islam", Gender.MALE.name(), 33).getRow());
            batch.add(new Passenger("Sumaiya Islam", Gender.FEMALE.name(), 24).getRow());
            batch.add(new Passenger("Tasnim Islam Khan", Gender.FEMALE.name(), 19).getRow());
            batch.add(new Passenger("Tanvir Islam", Gender.MALE.name(), 33).getRow());
            batch.add(new Passenger("Sumaiya Islam", Gender.FEMALE.name(), 24).getRow());
            batch.add(new Passenger("Sohana Islam Khan", Gender.FEMALE.name(), 27).getRow());
            batch.add(new Passenger("Tanvir Islam Akand", Gender.MALE.name(), 33).getRow());
            batch.add(new Passenger("Sumaiya Islam", Gender.FEMALE.name(), 24).getRow());
            batch.add(new Passenger("Tasnim Islam Khan", Gender.FEMALE.name(), 19).getRow());
            batch.add(new Passenger("Tanvir Islam", Gender.MALE.name(), 33).getRow());
            //Insert In Batch:
            Entity.insert(Passenger.class, executor, 5, batch);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private int getCount(Class<? extends Entity> entityType, SQLExecutor executor) throws SQLException {
        SQLScalarQuery query = new SQLQuery.Builder(QueryType.COUNT)
                .columns()
                .from(entityType)
                .build();
        int totalFound = executor.getScalarValue(query);
        return totalFound;
    }

    @Test
    public void CreateInBatchTest() throws SQLException {
        int count = getCount(Passenger.class, exe);
        Assert.assertTrue(count == 0);
        System.out.println("Before COUNT: " + count);
        //
        PerformanceLogger logger = new PerformanceLogger();
        insertSeedPassengerInBatch(exe);
        logger.printMillis("InsertInBatch: ");
        //
        count = getCount(Passenger.class, exe);
        Assert.assertTrue(count > 0);
        System.out.println("After COUNT: " + count);
    }

    @Test
    public void ReadAsyncTest() {
        //Seeding:
        insertSeedPassengerInBatch(exe);
        //
        PerformanceLogger logger = new PerformanceLogger();
        Predicate clause = new Where("name").isLike("%Islam%");
        Entity.read(Passenger.class, exe, 5, clause, (pass) -> {
            //Printing: Page By Page
            System.out.println("==============================================");
            pass.stream().map(Passenger::getName).forEach(System.out::println);
        });
        logger.printMillis("Read-Async: ");
    }

    @Test
    public void ReadTest() throws Exception {
        //Seeding:
        insertSeedPassengerInBatch(exe);
        //
        PerformanceLogger logger = new PerformanceLogger();
        Predicate clause = new Where("name").isLike("%Islam%");
        List<Passenger> pass = Entity.read(Passenger.class, exe, clause);
        pass.stream().map(Passenger::getName).forEach(System.out::println);
        logger.printMillis("Read-Sync: ");
    }

    @Test
    public void UpdateTest() {
        PerformanceLogger logger = new PerformanceLogger();
        //
        logger.printMillis("Update: ");
    }

    @Test
    public void DeleteTest() {
        PerformanceLogger logger = new PerformanceLogger();
        //
        logger.printMillis("Delete: ");
    }

    @Test
    public void CountTest() throws SQLException {
        //Seeding
        insertSeedPassenger(exe);
        //
        PerformanceLogger logger = new PerformanceLogger();
        int count = getCount(Passenger.class, exe);
        Assert.assertTrue(count > 0);
        logger.printMillis("Scalar: ");
    }

}
