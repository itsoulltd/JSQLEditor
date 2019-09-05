package com.it.soul.lab.jpql.query;

import com.it.soul.lab.jpql.entity.JPerson;
import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.models.Predicate;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.Row;
import com.it.soul.lab.sql.query.models.Where;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

public class QueryTest {


    @Test
    public void insertTest(){
        Row insertRow = new Row().add("name","towhid")
                .add("age", 36)
                .add("createDate", new Timestamp(new Date().getTime()));
        //
        JPQLInsertQuery insert = new JPQLQuery.Builder(QueryType.INSERT)
                .into(Entity.tableName(JPerson.class))
                .values(insertRow.getCloneProperties().toArray(new Property[0]))
                .build();
        //
        System.out.println(insert.toString());
    }

    @Test
    public void updateTest(){
        Predicate where = new Where("name").isEqualTo("towhid");
        //
        Row update = new Row().add("age", 40)
                .add("createDate", new Timestamp(new Date().getTime()));
        //
        JPQLUpdateQuery updateQuery = new JPQLQuery.Builder(QueryType.UPDATE)
                .set(update.getCloneProperties().toArray(new Property[0]))
                .from(Entity.tableName(JPerson.class))
                .where(where)
                .build();
        //
        System.out.println(updateQuery.toString());
    }

    @Test
    public void deleteTest(){
        Predicate where = new Where("name").isEqualTo("towhid");
        JPQLDeleteQuery query = new JPQLQuery.Builder(QueryType.DELETE)
                .rowsFrom(Entity.tableName(JPerson.class))
                .where(where).build();
        System.out.println(query.toString());
    }

}
