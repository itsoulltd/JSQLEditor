package com.it.soul.lab.jpql.service;

import com.it.soul.lab.jpql.entity.JPassenger;
import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.models.Predicate;
import com.it.soul.lab.sql.query.models.Where;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class JPassengerTest{

    enum Sex{
        Male,
        Female,
        Other
    }

    String[] names = new String[]{"Sohana","Mr.Towhid","Mr.Tanvir","Sumaiya","Tusin"};
    Integer[] ages = new Integer[] {15, 18, 28, 26, 32, 34, 25, 67};
    String password = "root";
    ORMServiceExecutor<JPassenger> executor;

    @Before
    public void before(){
        ORMController controller = new ORMController("testDB");
        executor = new ORMServiceExecutor<>(controller.getEntityManager(), JPassenger.class);
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
        Random random = new Random();
        int index_age = random.nextInt(ages.length -2) + 1;
        int index_name = random.nextInt(names.length -2) + 1;
        JPassenger passenger = new JPassenger();
        passenger.setUuid(UUID.randomUUID().toString());
        passenger.setAge(ages[index_age]);
        passenger.setName(names[index_name]);
        if (passenger.getName().toLowerCase().startsWith("mr")){
            passenger.setSex(Sex.Male.name());
        }else {
            passenger.setSex(Sex.Female.name());
        }
        passenger.insert(executor);
        //Read
        Predicate where = new Where("sex").isEqualTo("Male");
        List<JPassenger> readed = Entity.read(JPassenger.class, executor, where);
        if (readed != null) readed.forEach(rperson -> System.out.println(rperson.marshallingToMap(false)));
        //
    }
}
