package com.it.soul.lab.sql.query.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class PropertyTest {

    @Test
    public void compareTest(){

        //Property.getValue() is null:
        Property property = new Property();
        Property property1 = new Property();
        System.out.println(property1.compareTo(property));
        Assert.assertTrue(property1.compareTo(property) == -1);

        //Property.getType() miss-match:
        property.setValue(12);
        property1.setValue(12.0);
        System.out.println(property1.compareTo(property));
        Assert.assertTrue(property1.compareTo(property) == -1);

        property.setValue(12);
        property1.setValue(13);
        System.out.println(property1.compareTo(property));
        Assert.assertTrue(property1.compareTo(property) != 0);

        property.setValue(true);
        property1.setValue(true);
        System.out.println(property1.compareTo(property));
        Assert.assertTrue(property1.compareTo(property) == 0);

        property.setValue("2020-11-29 12:10:58");
        property.setType(DataType.SQLDATE);
        property1.setValue("2020-11-29 12:10:59");
        property1.setType(DataType.SQLDATE);
        System.out.println(property1.compareTo(property));
        Assert.assertTrue(property1.compareTo(property) != 0);

        /*Date date = new Date();
        property.setValue(date);
        property1.setValue(date);
        System.out.println(property1.compareTo(property));
        Assert.assertTrue(property1.compareTo(property) == 0);*/

    }

    @Test
    public void equalTest(){
        Property property = new Property("myProp");
        Property property1 = new Property("myProp");
        System.out.println(property1.equals(property));
        Assert.assertTrue(!property1.equals(property));

        property.setValue(12);
        property1.setValue(12);
        System.out.println(property1.equals(property));
        Assert.assertTrue(property1.equals(property));

        property.setValue(12.9);
        property1.setValue(12.9);
        System.out.println(property1.equals(property));
        Assert.assertTrue(property1.equals(property));

        property.setValue(12.8);
        property1.setValue(12.9);
        System.out.println(property1.equals(property));
        Assert.assertTrue(!property1.equals(property));

        property.setValue(true);
        property1.setValue(true);
        System.out.println(property1.equals(property));
        Assert.assertTrue(property1.equals(property));

        property.setValue(true);
        property1.setValue(false);
        System.out.println(property1.equals(property));
        Assert.assertTrue(!property1.equals(property));

        //String
        property.setValue("2020-11-29");
        property1.setValue("2020-11-29");
        System.out.println(property1.equals(property));
        Assert.assertTrue(property1.equals(property));

        //Date
        property.setValue("2020-11-29");
        property.setType(DataType.SQLDATE);
        property1.setValue("2020-11-29");
        property1.setType(DataType.SQLDATE);
        System.out.println(property1.equals(property));
        Assert.assertTrue(property1.equals(property));

        //DateTime
        property.setValue("2020-11-29 12:10:59");
        property.setType(DataType.SQLDATE);
        property1.setValue("2020-11-29 12:10:59");
        property1.setType(DataType.SQLDATE);
        System.out.println(property1.equals(property));
        Assert.assertTrue(property1.equals(property));

        property.setValue("2020-11-29 12:10:58");
        property.setType(DataType.SQLDATE);
        property1.setValue("2020-11-29 12:10:59");
        property1.setType(DataType.SQLDATE);
        System.out.println(property1.equals(property));
        Assert.assertTrue(!property1.equals(property));

        //Timestamp
        property.setValue("2020-11-29 12:10:59");
        property.setType(DataType.SQLTIMESTAMP);
        property1.setValue("2020-11-29 12:10:59");
        property1.setType(DataType.SQLTIMESTAMP);
        System.out.println(property1.equals(property));
        Assert.assertTrue(property1.equals(property));

        property.setValue("2020-11-29 12:10:58");
        property.setType(DataType.SQLTIMESTAMP);
        property1.setValue("2020-11-29 12:10:59");
        property1.setType(DataType.SQLTIMESTAMP);
        System.out.println(property1.equals(property));
        Assert.assertTrue(!property1.equals(property));
    }

    @Test
    public void hashTest(){
        Property property = new Property();
        System.out.println(property.hashCode());

        Property property1 = new Property();
        System.out.println(property1.hashCode());
        Assert.assertTrue(property.hashCode() == property1.hashCode());

        Property property2 = new Property("k", 12);
        System.out.println(property2.hashCode());
        Assert.assertTrue(property1.hashCode() != property2.hashCode());

    }

    @Test
    public void propTestBigDecimal() throws JsonProcessingException {
        Property property = new Property("amount", new BigDecimal(100.00));
        String str = property.toString();
        System.out.println("Actual: "+ str);
        //
        ObjectMapper mapper = new ObjectMapper();
        Property reProp = mapper.readValue(str, Property.class);
        System.out.println("Expected: " + reProp.toString());
        //
        Assert.assertEquals(reProp.toString(), property.toString());
    }

    @Test
    public void propTestString() throws JsonProcessingException {
        Property property = new Property("amount", "Hi There");
        String str = property.toString();
        System.out.println("Actual: "+ str);
        //
        ObjectMapper mapper = new ObjectMapper();
        Property reProp = mapper.readValue(str, Property.class);
        System.out.println("Expected: " + reProp.toString());
        //
        Assert.assertEquals(reProp.toString(), property.toString());
    }

    @Test
    public void propTestInteger() throws JsonProcessingException {
        Property property = new Property("amount", 10000);
        String str = property.toString();
        System.out.println("Actual: "+ str);
        //
        ObjectMapper mapper = new ObjectMapper();
        Property reProp = mapper.readValue(str, Property.class);
        System.out.println("Expected: " + reProp.toString());
        //
        Assert.assertEquals(reProp.toString(), property.toString());
    }

    @Test
    public void propTestDouble() throws JsonProcessingException {
        Property property = new Property("amount", 2.89d);
        String str = property.toString();
        System.out.println("Actual: "+ str);
        //
        ObjectMapper mapper = new ObjectMapper();
        Property reProp = mapper.readValue(str, Property.class);
        System.out.println("Expected: " + reProp.toString());
        //
        Assert.assertEquals(reProp.toString(), property.toString());
    }

    @Test
    public void propTestFloat() throws JsonProcessingException {
        Property property = new Property("amount", 2.3f);
        String str = property.toString();
        System.out.println("Actual: "+ str);
        //
        ObjectMapper mapper = new ObjectMapper();
        Property reProp = mapper.readValue(str, Property.class);
        System.out.println("Expected: " + reProp.toString());
        //
        Assert.assertEquals(reProp.toString(), property.toString());
    }

    @Test
    public void propTestBool() throws JsonProcessingException {
        Property property = new Property("isAmount", true);
        String str = property.toString();
        System.out.println("Actual: "+ str);
        //
        ObjectMapper mapper = new ObjectMapper();
        Property reProp = mapper.readValue(str, Property.class);
        System.out.println("Expected: " + reProp.toString());
        //
        Assert.assertEquals(reProp.toString(), property.toString());
    }

}