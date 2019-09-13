package com.it.soul.lab.sql;

import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.entity.Ignore;
import org.junit.Assert;
import org.junit.Test;

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
    }

}
