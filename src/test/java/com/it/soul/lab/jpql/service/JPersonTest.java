package com.it.soul.lab.jpql.service;

import com.it.soul.lab.jpql.entity.JPerson;
import com.it.soul.lab.sql.entity.Entity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class JPersonTest {

    String[] names = new String[]{"Sohana","Towhid","Tanvir","Sumaiya","Tusin"};
    Integer[] ages = new Integer[] {15, 18, 28, 26, 32, 34, 25, 67};
    String password = "root";
    JPQLExecutor executor;

    @Before
    public void before(){
        ORMController controller = new ORMController("testDB");
        executor = new JPQLExecutor(controller.getEntityManager());
    }

    @After
    public void after(){
        try {
            if(executor != null) executor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void personTest() throws Exception {
        JPerson person = new JPerson();
        person.setAge(36);
        person.setActive(true);
        person.setName("towhid");
        person.setCreateDate(new Timestamp(new Date().getTime()));
        person.setModifyDate(new Timestamp(new Date().getTime()));
        person.insert(executor);
        //Read
        List<JPerson> readed = Entity.read(JPerson.class, executor);
        if (readed != null) readed.forEach(rperson -> System.out.println(rperson.marshallingToMap(false)));
        //
    }

}
