package com.it.soul.lab.sql;

import com.it.soul.lab.connect.DriverClass;
import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.*;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.Row;
import com.it.soul.lab.sql.query.models.Where;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

public class SQLExecutorBatchTest {

    private SQLExecutor exe;
    String password = "root";

    String[] names = new String[]{"Sohana","Towhid","Tanvir","Sumaiya","Tusin"};
    Integer[] ages = new Integer[] {15, 18, 28, 26, 32, 34, 25, 67};

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

    @Before
    public void setUp() throws Exception {
        exe = new SQLExecutor.Builder(DriverClass.MYSQL)
                .database("testDB").credential("root", password).build();
    }

    @After
    public void tearDown() throws Exception {
        exe.close();
        exe = null;
        //clearAll();
    }

    //@Test
    public void seeAll(){
        SQLSelectQuery selectQuery = new SQLQuery.Builder(QueryType.SELECT)
                .columns().from("Passenger")
                .build();
        try {
            List<Passenger> all = exe.collection(exe.executeSelect(selectQuery)).inflate(Passenger.class);
            //all.forEach(passenger -> System.out.println(passenger.getName()));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updateQueryValueBinding(){
        SQLUpdateQuery updateQuery = new SQLQuery.Builder(QueryType.UPDATE)
                .set(new Property("name", "Towhid"), new Property("age", 36), new Property("sex", "male"))
                .from("Passenger")
                .where(new Where("uuid").notNull())
                .build();
        String buffer = updateQuery.bindValueToString();
        System.out.println(buffer);
        //
        updateQuery = new SQLQuery.Builder(QueryType.UPDATE)
                .set(new Property("name", "Towhid"), new Property("age", 36), new Property("sex", "male"))
                .from("Passenger")
                .where(new Where("uuid").isEqualTo(UUID.randomUUID().toString()))
                .build();
        buffer = updateQuery.bindValueToString();
        System.out.println(buffer);
        //
        SQLInsertQuery insertQuery = new SQLQuery.Builder(QueryType.INSERT)
                .into("Passenger")
                .values(new Property("name", "Towhid"), new Property("age", 36), new Property("sex", "male"))
                .build();
        buffer = insertQuery.bindValueToString();
        System.out.println(buffer);
    }

    @Test
    public void executeUpdate() {
        SQLSelectQuery selectQuery = new SQLQuery.Builder(QueryType.SELECT)
                .columns().from("Passenger")
                //.where(new Where("name").isLike("%tan%"))
                .build();
        try {
            List<Row> rows = new ArrayList<>();
            List<Passenger> passengers = exe.collection(exe.executeSelect(selectQuery)).inflate(Passenger.class);
            passengers.forEach(passenger -> {
                //System.out.println("Before: " + passenger.getName());
                Row row = new Row().add("name", getRandomName());
                rows.add(row);
            });
            //
            SQLUpdateQuery updateQuery = new SQLQuery.Builder(QueryType.UPDATE)
                    .set(new Property("name"))
                    .from(Passenger.tableName(Passenger.class)).build();
            //
            Integer[] updated = exe.executeUpdate(100, updateQuery, rows);
            System.out.println(updated.length > 0 ? "Successfully Updated "+ updated.length : "Failed To Update any.");
            seeAll();
            //
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void executeUpdateV2() {
        SQLSelectQuery selectQuery = new SQLQuery.Builder(QueryType.SELECT)
                .columns().from("Passenger")
                //.where(new Where("name").isLike("%tan%"))
                .build();
        try {
            List<SQLUpdateQuery> queries = new ArrayList<>();
            List<Passenger> passengers = exe.collection(exe.executeSelect(selectQuery)).inflate(Passenger.class);
            passengers.forEach(passenger -> {
                //System.out.println("Before: " + passenger.getName());
                //
                SQLUpdateQuery updateQuery = new SQLQuery.Builder(QueryType.UPDATE)
                        .set(new Property("name", passenger.getName() + "_updated_v2"))
                        .from(Passenger.tableName(Passenger.class)).build();
                queries.add(updateQuery);
            });
            //
            Integer[] updated = exe.executeUpdate(100, queries);
            System.out.println(updated.length > 0 ? "Successfully Updated "+ updated.length : "Failed To Update any.");
            seeAll();
            //
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void clearAll() {
        SQLDeleteQuery query = new SQLQuery.Builder(QueryType.DELETE)
                .rowsFrom(Passenger.tableName(Passenger.class))
                .where(new Where("name").notNull())
                .build();
        try {
            if(exe.executeDelete( query) > 0) System.out.println("successfully deleted");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void executeInsert() {
        SQLInsertQuery insertQuery = new SQLQuery.Builder(QueryType.INSERT)
                .into(Passenger.tableName(Passenger.class))
                .values(new Property("name"), new Property("age"), new Property("sex"))
                .build();
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < 15; i++){
            Row row = new Row().add("name",getRandomName()).add("age",getRandomAge()).add("sex","---");
            rows.add(row);
        }
        try {
            Integer[] inserted = exe.executeInsert(true, 100, insertQuery, rows);
            System.out.println(inserted.length > 0 ? "Successfully Inserted "+ inserted.length : "Failed To Update any.");
            seeAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test @SuppressWarnings("Duplicates")
    public void inQueryTest(){
        SQLSelectQuery query = new SQLQuery.Builder(QueryType.SELECT)
                .columns()
                .from("Person")
                .where(new Where("name").isIn("Towhid", "tanvir", "UK"))
                .build();
        System.out.println(query.bindValueToString());

        try {
            List<Person> personList = exe.executeSelect(query.bindValueToString(), Person.class);
            personList.forEach(person -> System.out.println(person.getAge()));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        query = new SQLQuery.Builder(QueryType.SELECT)
                .columns()
                .from("Person")
                .where(new Where("age").isIn(18, 65, 90))
                .build();
        System.out.println(query.bindValueToString());

        try {
            List<Person> personList = exe.executeSelect(query.bindValueToString(), Person.class);
            //personList.forEach(person -> System.out.println(person.getName_test()));
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}