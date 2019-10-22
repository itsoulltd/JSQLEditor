package com.it.soul.lab.jpql.service;

import com.it.soul.lab.jpql.entity.JPassenger;
import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.models.Predicate;
import com.it.soul.lab.sql.query.models.Where;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
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
    JPQLExecutor executor;

    @Before
    public void before(){
        ORMController controller = new ORMController("testDB");
        executor = new JPQLExecutor(controller.getEntityManager());
    }

    @After
    public void after(){
        try {
            if(executor != null)
                executor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    @Test
    public void passengerInsertTest() throws Exception {
        //
        JPassenger passenger = new JPassenger();
        passenger.setUuid(UUID.randomUUID().toString());
        //
        passenger.setAge(getRandomAge());
        passenger.setName(getRandomName());
        //
        if (passenger.getName().toLowerCase().startsWith("mr")){
            passenger.setSex(Sex.Male.name());
        }else {
            passenger.setSex(Sex.Female.name());
        }
        //Insertion
        long countBeforeInsert = executor.rowCount(JPassenger.class);
        boolean isInserted = passenger.insert(executor);
        long countAfterInsert = executor.rowCount(JPassenger.class);
        Assert.assertTrue("Insert Failed!", isInserted && (countBeforeInsert < countAfterInsert));
    }

    @Test
    public void passengerUpdateTest() throws Exception {
        //
        Predicate findNameBy = new Where("name").isEqualTo(getRandomName());
        //
        List<JPassenger> byName = Entity.read(JPassenger.class, executor, findNameBy);
        //
        String updatedUid = null;
        if (byName != null && byName.size() > 0){
            JPassenger passenger1 = byName.get(0);
            passenger1.setName(passenger1.getName() + "_updated");
            //Update
            boolean isUpdated = passenger1.update(executor);
            updatedUid = passenger1.getUuid();
            Assert.assertTrue("Update Failed!", isUpdated);
        }
        //
        if (updatedUid != null && !updatedUid.isEmpty()) {
            Predicate likeWise = new Where("uuid").isLike(updatedUid);
            List<JPassenger> retrieved = Entity.read(JPassenger.class, executor, likeWise);
            //if (retrieved != null) retrieved.forEach(passenger -> System.out.println(passenger.marshallingToMap(false)));
        }
    }
    
    @Test
    public void passengerUpdateThoseWhoSexIsNullTest() throws Exception {
        //Update
        //"SELECT c FROM Concept c WHERE c.conceptName = :conceptName and c.refTable IS NULL"
        Predicate isNUll = new Where("sex").isNull();
        List<JPassenger> sexIsNull = Entity.read(JPassenger.class, executor, isNUll);
        if (sexIsNull != null && sexIsNull.size() > 0){
            sexIsNull.forEach(jPassenger -> {
                try {
                    if (jPassenger.getName() == null) return;
                    if (jPassenger.getName().toLowerCase().startsWith("mr")){
                        jPassenger.setSex(Sex.Male.name());
                    }else {
                        jPassenger.setSex(Sex.Female.name());
                    }
                    jPassenger.update(executor);
                    //System.out.println("Updated: " + jPassenger.marshallingToMap(false));
                } catch (SQLException e) {
                }
            });
        }
        //
    }

    @Test
    public void passengerDeleteTest() throws Exception {
        //
        Predicate findNameBy = new Where("name").isEqualTo(getRandomName());
        //
        List<JPassenger> byName = Entity.read(JPassenger.class, executor, findNameBy);
        String goingToDeleteId = null;
        //
        if (byName != null && byName.size() > 0){
            JPassenger passenger1 = byName.get(0);
            goingToDeleteId = passenger1.getUuid();
            //Delete
            boolean isDeleted = passenger1.delete(executor);
            Assert.assertTrue("Deletion Failed!", isDeleted);
        }
        //
        if (goingToDeleteId != null && !goingToDeleteId.isEmpty()) {
            Predicate likeWise = new Where("uuid").isLike(goingToDeleteId);
            List<JPassenger> retrieved = Entity.read(JPassenger.class, executor, likeWise);
            //if (retrieved == null || retrieved.size() <= 0) System.out.println(goingToDeleteId + " has been successfully deleted.");
        }
    }

    @Test
    public void passengerReadTest() throws Exception {
        //Read
        Predicate where = new Where("sex").isEqualTo("Male")
                .and("age").isGreaterThenOrEqual(30);
        List<JPassenger> retrieved = Entity.read(JPassenger.class, executor, where);
        /*if (retrieved != null)
            retrieved.forEach(passenger -> System.out.println(passenger.marshallingToMap(false)));*/
    }

    /*@Test
    public void passengerReadAllTest() throws Exception {
        //Read All
        List<JPassenger> retrieved = Entity.read(JPassenger.class, executor);
        if (retrieved != null)
            retrieved.forEach(passenger -> System.out.println(passenger.marshallingToMap(false)));
    }*/

    @Test
    public void notNullQueryTest() throws Exception {
        //Checking isNotNull query
        Predicate notNullQuery = new Where("sex").notNull()
                .and("name").notNull();
        List<JPassenger> notNull = Entity.read(JPassenger.class, executor, notNullQuery);
        /*if (notNull != null)
            notNull.forEach(jPassenger -> System.out.println(jPassenger.getName() + "#" + jPassenger.getSex()));*/
    }

    //@Test
    public void transactionTest(){
        //
        int rand = (new Random()).nextInt(2);
        boolean shouldFailed = rand >= 1;
        //
        try {
            executor.begin();
            System.out.println("begin()");
            JPassenger passenger = new JPassenger();
            passenger.setUuid(UUID.randomUUID().toString());
            passenger.setName(getRandomName());
            System.out.println("Name: " + passenger.getName());
            passenger.setAge(getRandomAge());
            passenger.insert(executor);
            try{
                Thread.sleep(5000);
            }catch (Exception e) {
                System.out.println(e.getMessage());
            }
            if (shouldFailed) {
                throw new RuntimeException("Going To Failed!!!");
            }
            executor.end();
            System.out.println("end()");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            try {
                executor.abort();
                System.out.println("abort()");
            } catch (Exception e1) {System.out.println(e1.getMessage());}
        } finally {
            //
        }
    }

}
