package com.it.soul.lab.data.simple;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleDataSourceTest {

    SimpleDataSource<String, Object> dataSource;
    SimpleDataSource<Integer, Object> intDataSource;

    private void loadDataSource(){
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

        dataSource.put("p-7", new Person()
                .setName("Lut")
                .setEmail("lut@gmail.com")
                .setAge(32)
                .setGender("male"));
    }

    private void loadIntDataSource(){
        intDataSource = new SimpleDataSource<>();

        intDataSource.add(new Person()
                .setName("John")
                .setEmail("john@gmail.com")
                .setAge(36)
                .setGender("male"));

        intDataSource.add(new Person()
                .setName("Eve")
                .setEmail("eve@gmail.com")
                .setAge(21)
                .setGender("female"));

        intDataSource.add(new Person()
                .setName("Mosses")
                .setEmail("mosses@gmail.com")
                .setAge(31)
                .setGender("male"));

        intDataSource.add(new Person()
                .setName("Abraham")
                .setEmail("abraham@gmail.com")
                .setAge(31)
                .setGender("male"));

        intDataSource.add(new Person()
                .setName("Ahmed")
                .setEmail("ahmed@gmail.com")
                .setAge(31)
                .setGender("male"));

        intDataSource.add(new Person()
                .setName("Adam")
                .setEmail("adam@gmail.com")
                .setAge(31)
                .setGender("male"));
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void readSyncTest() {
        //Load sample data:
        loadDataSource();

        //ReadSync and Convert:
        int maxItem = dataSource.size();
        Object[] items = dataSource.readSync(0, maxItem);
        List<Person> converted = Stream.of(items).map(itm -> (Person) itm).collect(Collectors.toList());
        converted.forEach(person -> System.out.println(person.toString()));
    }

    @Test
    public void readAsyncTest() throws InterruptedException {
        //Load sample data:
        loadDataSource();
        CountDownLatch latch = new CountDownLatch(1);

        //ReadAsync and Convert:
        int maxItem = dataSource.size();
        dataSource.readAsync(0, maxItem, (items) -> {
            List<Person> converted = Stream.of(items).map(itm -> (Person) itm).collect(Collectors.toList());
            converted.forEach(person -> System.out.println(person.toString()));
            latch.countDown();
        });

        //Blocking until async-returns:
        latch.await(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void readTest() {
        //
        loadDataSource();
        //
        System.out.println("===========================0-(datasource.size())======================");
        int maxItem = dataSource.size();
        Object[] readAll = dataSource.readSync(0, maxItem);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("===========================1-2==========================");
        readAll = dataSource.readSync(1, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("===========================2-3=========================");
        readAll = dataSource.readSync(2, 3);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("===========================0-3=========================");
        readAll = dataSource.readSync(0, 3);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("===========================0-2========================");
        readAll = dataSource.readSync(0, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("==========================100-10=======================");
        readAll = dataSource.readSync(100, 10);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("==========================0-0=======================");
        readAll = dataSource.readSync(0, 0);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("==========================1-0=======================");
        readAll = dataSource.readSync(1, 0);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("==========================(Asynch)===================");
        dataSource.readAsync(4, 1, (items) -> {
            System.out.println("==========================4-1=======================");
            for (Object p : items) {
                System.out.println(p.toString());
            }
        });
        //
        dataSource.readAsync(4, 2, (items) -> {
            System.out.println("==========================4-2=======================");
            for (Object p : items) {
                System.out.println(p.toString());
            }
        });
        //
        dataSource.readAsync(4, 3, (items) -> {
            System.out.println("==========================4-3=======================");
            for (Object p : items) {
                System.out.println(p.toString());
            }
        });
    }

    @Test
    public void addTest() {
        //
        loadIntDataSource();
        System.out.println(intDataSource.size());
        //
        System.out.println("===========================0-(datasource.size())======================");
        int maxItem = intDataSource.size();
        Object[] readAll = intDataSource.readSync(0, maxItem);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("===========================1-2==========================");
        readAll = intDataSource.readSync(1, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("===========================2-3=========================");
        readAll = intDataSource.readSync(2, 3);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        System.out.println("===========================0-3=========================");
        readAll = intDataSource.readSync(0, 3);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
    }

    @Test
    public void additionFuncTest() {

        ////When String is Key:
        SimpleDataSource<String, Person> dataSource = new SimpleDataSource<>();

        dataSource.put("p-1", new Person()
                .setName("John")
                .setEmail("john@gmail.com")
                .setAge(36)
                .setGender("male"));

        dataSource.add(new Person()
                .setName("Abraham")
                .setEmail("Abraham@gmail.com")
                .setAge(45)
                .setGender("male"));

        System.out.println("===========================0-2==========================");
        Object[] readAll = dataSource.readSync(0, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }

        ////When Integer is Key:
        SimpleDataSource<Integer, Person> intDataSource = new SimpleDataSource<>();

        intDataSource.put(0, new Person()
                .setName("John")
                .setEmail("john@gmail.com")
                .setAge(36)
                .setGender("male"));

        Integer intKey = intDataSource.add(new Person()
                .setName("Abraham")
                .setEmail("Abraham@gmail.com")
                .setAge(45)
                .setGender("male"));

        System.out.println("===========================1-1==========================");
        Person person = intDataSource.read(intKey);
        System.out.println(person.toString());
        //
        System.out.println("");
    }

    @Test
    public void addDeleteTests() {
        SimpleDataSource<String, Person> dataSource = new SimpleDataSource<>();

        Person a = new Person()
                .setName("John")
                .setEmail("john@gmail.com")
                .setAge(36)
                .setGender("male");

        Person b = new Person()
                .setName("Abraham")
                .setEmail("Abraham@gmail.com")
                .setAge(45)
                .setGender("male");

        if(!dataSource.contains(a)) dataSource.add(a);
        if(!dataSource.contains(b)) dataSource.add(b);

        System.out.println("===========================0-2==========================");
        Object[] readAll = dataSource.readSync(0, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }

        System.out.println("Size before delete: " + dataSource.size());
        if(dataSource.contains(a)) dataSource.delete(a);
        System.out.println("Size after delete: " + dataSource.size());

    }

    public int getOffset(int page, int limit) {
        if (limit <= 0) limit = 10;
        if (page <= 0) page = 1;
        int offset = (page - 1) * limit;
        return offset;
    }

    @Test
    public void pageVsOffsetTest() {
        //Seed:
        int limit = 10;
        int offset = 0;

        //Example-01
        offset = getOffset(2, limit);
        Assert.assertEquals(10, offset);
        System.out.println("When (limit:10 & page:2) Offset expected: 10; actual: " + offset);

        //Example-02
        offset = getOffset(-1, limit);
        Assert.assertEquals(0, offset);
        System.out.println("When (limit:10 & page:-1) Offset expected: 0; actual: " + offset);

        //Example-03
        offset = getOffset(7, limit);
        Assert.assertEquals(60, offset);
        System.out.println("When (limit:10 & page:7) Offset expected: 60; actual: " + offset);

        //Example-04
        offset = getOffset(101, limit);
        Assert.assertEquals(1000, offset);
        System.out.println("When (limit:10 & page:101) Offset expected: 1000; actual: " + offset);

        //Example-05
        offset = getOffset(2, 15);
        Assert.assertEquals(15, offset);
        System.out.println("When (limit:15 & page:2) Offset expected: 15; actual: " + offset);

        //Example-06
        offset = getOffset(-1, 15);
        Assert.assertEquals(0, offset);
        System.out.println("When (limit:15 & page:-1) Offset expected: 0; actual: " + offset);

        //Example-07
        offset = getOffset(7, 20);
        Assert.assertEquals(120, offset);
        System.out.println("When (limit:20 & page:7) Offset expected: 120; actual: " + offset);

        //Example-08
        offset = getOffset(-1, -1);
        Assert.assertEquals(0, offset);
        System.out.println("When (limit:-1 & page:-1) Offset expected: 0; actual: " + offset);
    }

    @Test
    public void paginationTest() {
        //Load Data:
        loadDataSource();
        //
        int maxItem = dataSource.size();
        System.out.println("===========================ALL==========================");
        Object[] items = dataSource.readSync(0, maxItem);
        List<Person> converted = Stream.of(items).map(itm -> (Person) itm).collect(Collectors.toList());
        converted.forEach(person -> System.out.println(person.toString()));
        //
        System.out.println("===========================Page:0-limit:2==========================");
        int offset = getOffset(0, 2);
        Object[] readAll = dataSource.readSync(offset, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        //
        System.out.println("===========================Page:1-limit:2==========================");
        offset = getOffset(1, 2);
        readAll = dataSource.readSync(offset, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        //
        System.out.println("===========================Page:2-limit:2==========================");
        offset = getOffset(2, 2);
        readAll = dataSource.readSync(offset, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        //
        System.out.println("===========================Page:3-limit:2==========================");
        offset = getOffset(3, 2);
        readAll = dataSource.readSync(offset, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        //
        System.out.println("===========================Page:4-limit:2==========================");
        offset = getOffset(4, 2);
        readAll = dataSource.readSync(offset, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
        //
        System.out.println("===========================Page:5-limit:2==========================");
        offset = getOffset(5, 2);
        readAll = dataSource.readSync(offset, 2);
        for (Object p : readAll) {
            System.out.println(p.toString());
        }
    }

}