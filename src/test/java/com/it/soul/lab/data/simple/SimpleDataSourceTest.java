package com.it.soul.lab.data.simple;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SimpleDataSourceTest {

    SimpleDataSource<String, Person> dataSource;

    @Before
    public void setUp() throws Exception {

        dataSource = new SimpleDataSource<>();

        dataSource.put("p-1", new Person()
                .setName("John")
                .setEmail("john@gmail.com")
                .setAge(36)
                .setGender("male"));

        dataSource.put("p-2", new Person()
                .setName("Eve")
                .setEmail("eve@gmail.com")
                .setAge(21)
                .setGender("female"));

        dataSource.put("p-3", new Person()
                .setName("Mosses")
                .setEmail("mosses@gmail.com")
                .setAge(31)
                .setGender("male"));

        dataSource.put("p-4", new Person()
                .setName("Abraham")
                .setEmail("abraham@gmail.com")
                .setAge(31)
                .setGender("male"));

        dataSource.put("p-5", new Person()
                .setName("Ahmed")
                .setEmail("ahmed@gmail.com")
                .setAge(31)
                .setGender("male"));

        dataSource.put("p-6", new Person()
                .setName("Adam")
                .setEmail("adam@gmail.com")
                .setAge(31)
                .setGender("male"));
    }

    @After
    public void tearDown() throws Exception {
        dataSource = null;
    }

    @Test
    public void readTest(){

        System.out.println("===========================0-(datasource.size())======================");
        int maxItem = Long.valueOf(dataSource.size()).intValue();
        Object[] readAll = dataSource.readSynch(0, maxItem);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("===========================1-2==========================");
        readAll = dataSource.readSynch(1, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("===========================2-3=========================");
        readAll = dataSource.readSynch(2, 3);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("===========================0-3=========================");
        readAll = dataSource.readSynch(0, 3);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("===========================0-2========================");
        readAll = dataSource.readSynch(0, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("==========================100-10=======================");
        readAll = dataSource.readSynch(100, 10);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("==========================0-0=======================");
        readAll = dataSource.readSynch(0, 0);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("==========================1-0=======================");
        readAll = dataSource.readSynch(1, 0);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("==========================4-1=======================");
        readAll = dataSource.readSynch(4, 1);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("==========================4-2=======================");
        readAll = dataSource.readSynch(4, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("==========================4-3=======================");
        readAll = dataSource.readSynch(4, 3);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
    }

}