package com.it.soul.lab.sql;

import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.entity.Ignore;
import com.it.soul.lab.sql.query.models.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EntityTest {

    @Test
    public void marshallTest(){

        Owner me = new Owner();
        me.name = "Towhid";
        me.age = 36;
        me.car = new Car();
        me.car.engineNo = UUID.randomUUID().toString();
        me.car.chassisNo = UUID.randomUUID().toString();
        me.car.countOfWheel = 4;
        me.car.body = new CarBody();
        me.car.body.cc = 1200.50;
        me.car.body.madeOf = "Steel";
        me.car.body.numberOfDoors = null;

        Map<String, Object> data = me.marshallingToMap(true);
        //Map<String, Object> carData = (Map<String, Object>) data.get("car");

        //Create a New Owner form data
        Owner nOwner = new Owner();
        nOwner.unmarshallingFromMap(data,true);
        Car nCar = nOwner.getCar();
        //
        Assert.assertEquals("Both Car Must Be same", me.car, nCar);
        Assert.assertEquals("Both Owner Must Be same! Too", me, nOwner);
    }

    @Test
    public void pageCountTest(){
        int rowCount = 100;
        int pageSize = 10;
        int loopCount = (rowCount % 2 == 0) ? (rowCount / pageSize) : ((rowCount / pageSize) + 1);
        Assert.assertEquals(10, loopCount);

        rowCount = 100;
        pageSize = 9;
        loopCount = (rowCount % 2 == 0) ? (rowCount / pageSize) : ((rowCount / pageSize) + 1);
        Assert.assertEquals(11, loopCount);

        rowCount = 99;
        pageSize = 10;
        loopCount = (rowCount % 2 == 0) ? (rowCount / pageSize) : ((rowCount / pageSize) + 1);
        Assert.assertEquals(10, loopCount);

        rowCount = 99;
        pageSize = 9;
        loopCount = (rowCount % 2 == 0) ? (rowCount / pageSize) : ((rowCount / pageSize) + 1);
        Assert.assertEquals(12, loopCount);

        rowCount = 80856;
        pageSize = 1000;
        loopCount = (rowCount % 2 == 0) ? (rowCount / pageSize) : ((rowCount / pageSize) + 1);
        Assert.assertEquals(80, loopCount);

        //floor

        rowCount = 80856;
        pageSize = 1000;
        loopCount = Double.valueOf(Math.floor((rowCount * 1.0d / pageSize))).intValue();
        System.out.println("floor->>" + Math.floor((rowCount * 1.0d / pageSize)));
        Assert.assertEquals(80, loopCount);

        rowCount = 80855;
        pageSize = 1000;
        loopCount = Double.valueOf(Math.floor((rowCount * 1.0d / pageSize))).intValue();
        System.out.println("floor->>" + Math.floor((rowCount * 1.0d / pageSize)));
        Assert.assertEquals(80, loopCount);
    }

    @Test
    public void pageCountCeilTest(){
        //ceil

        int rowCount = 80856;
        int pageSize = 1000;
        int loopCount = Double.valueOf(Math.ceil((rowCount * 1.0d / pageSize))).intValue();
        System.out.println("ceil->>" + Math.ceil((rowCount * 1.0d / pageSize)));
        Assert.assertEquals(81, loopCount);

        rowCount = 80855;
        pageSize = 1000;
        loopCount = Double.valueOf(Math.ceil((rowCount * 1.0d / pageSize))).intValue();
        System.out.println("ceil->>" + Math.ceil((rowCount * 1.0d / pageSize)));
        Assert.assertEquals(81, loopCount);

        rowCount = 53;
        pageSize = 7;
        loopCount = Double.valueOf(Math.ceil((rowCount * 1.0d / pageSize))).intValue();
        System.out.println("ceil->>" + Math.ceil((rowCount * 1.0d / pageSize)));
        Assert.assertEquals(8, loopCount);

        rowCount = 50;
        pageSize = 7;
        loopCount = Double.valueOf(Math.ceil((rowCount * 1.0d / pageSize))).intValue();
        System.out.println("ceil->>" + Math.ceil((rowCount * 1.0d / pageSize)));
        Assert.assertEquals(8, loopCount);

        rowCount = 13;
        pageSize = 7;
        loopCount = Double.valueOf(Math.ceil((rowCount * 1.0d / pageSize))).intValue();
        System.out.println("ceil->>" + Math.ceil((rowCount * 1.0d / pageSize)));
        Assert.assertEquals(2, loopCount);

        rowCount = 14;
        pageSize = 7;
        loopCount = Double.valueOf(Math.ceil((rowCount * 1.0d / pageSize))).intValue();
        System.out.println("ceil->>" + Math.ceil((rowCount * 1.0d / pageSize)));
        Assert.assertEquals(2, loopCount);

        rowCount = 15;
        pageSize = 7;
        loopCount = Double.valueOf(Math.ceil((rowCount * 1.0d / pageSize))).intValue();
        System.out.println("ceil->>" + Math.ceil((rowCount * 1.0d / pageSize)));
        Assert.assertEquals(3, loopCount);
    }

    @Test public void rowDefTest(){
        Row def = Entity.getRowDefinition(Passenger.class);
        Assert.assertEquals("{DOB=null, SEX=null, ID=null, AGE=null, NAME=null}"
                                    , def.toString());
        System.out.println(def.toString());

        Row def2 = Entity.getRowDefinition(Owner.class);
        Assert.assertEquals("{car=null, name=null, age=null}"
                            , def2.toString());
        System.out.println(def2.toString());
    }

    @Test public void rowValueTest(){
        //
        Passenger passenger = new Passenger();
        passenger.setName("Pass");
        passenger.setAge(34);
        passenger.setSex("Male");
        Row passRow = passenger.getRow();
        Assert.assertEquals("{DOB=null, SEX=Male, ID=null, AGE=34, NAME=Pass}"
                , passRow.toString());
        System.out.println(passRow.toString());

        Row passRow2 = passenger.getRow("DOB");
        Assert.assertEquals("{SEX=Male, ID=null, AGE=34, NAME=Pass}"
                , passRow2.toString());
        System.out.println(passRow2.toString());

        Owner me = new Owner();
        me.name = "Hi Pass";
        me.age = 36;
        Row def2 = me.getRow();
        Assert.assertEquals("{car=null, name=Hi Pass, age=36}"
                , def2.toString());
        System.out.println(def2.toString());

        Row def3 = me.getRow("car");
        Assert.assertEquals("{name=Hi Pass, age=36}"
                , def3.toString());
        System.out.println(def3.toString());
    }

    public static class Owner extends Entity {
        @Ignore
        private static final long serialVersionUID = 1L;
        private String name;
        private int age;
        private Car car;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public Car getCar() {
            return car;
        }

        public void setCar(Car car) {
            this.car = car;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Owner owner = (Owner) o;
            return age == owner.age &&
                    Objects.equals(name, owner.name) &&
                    Objects.equals(car, owner.car);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age, car);
        }
    }

    public static class Car extends Entity {
        private String engineNo;
        private String chassisNo;
        private Integer countOfWheel;
        private CarBody body;

        public String getEngineNo() {
            return engineNo;
        }

        public void setEngineNo(String engineNo) {
            this.engineNo = engineNo;
        }

        public String getChassisNo() {
            return chassisNo;
        }

        public void setChassisNo(String chassisNo) {
            this.chassisNo = chassisNo;
        }

        public Integer getCountOfWheel() {
            return countOfWheel;
        }

        public void setCountOfWheel(Integer countOfWheel) {
            this.countOfWheel = countOfWheel;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Car car = (Car) o;
            return Objects.equals(engineNo, car.engineNo) &&
                    Objects.equals(chassisNo, car.chassisNo) &&
                    Objects.equals(countOfWheel, car.countOfWheel);
        }

        @Override
        public int hashCode() {
            return Objects.hash(engineNo, chassisNo, countOfWheel);
        }

        public CarBody getBody() {
            return body;
        }

        public void setBody(CarBody body) {
            this.body = body;
        }
    }

    public static class CarBody extends Entity{
        private String madeOf;
        private Integer numberOfDoors;
        private double cc;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CarBody carBody = (CarBody) o;
            return Double.compare(carBody.cc, cc) == 0 &&
                    Objects.equals(madeOf, carBody.madeOf) &&
                    Objects.equals(numberOfDoors, carBody.numberOfDoors);
        }

        @Override
        public int hashCode() {
            return Objects.hash(madeOf, numberOfDoors, cc);
        }

        public String getMadeOf() {
            return madeOf;
        }

        public void setMadeOf(String madeOf) {
            this.madeOf = madeOf;
        }

        public Integer getNumberOfDoors() {
            return numberOfDoors;
        }

        public void setNumberOfDoors(Integer numberOfDoors) {
            this.numberOfDoors = numberOfDoors;
        }

        public double getCc() {
            return cc;
        }

        public void setCc(double cc) {
            this.cc = cc;
        }
    }

}
