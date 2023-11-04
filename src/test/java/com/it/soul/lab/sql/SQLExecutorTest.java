package com.it.soul.lab.sql;

import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.models.Row;
import org.junit.Assert;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class SQLExecutorTest {

    protected void deleteSeed(SQLExecutor exe, Class<? extends Entity> aClass) throws SQLException {
        Entity.delete(aClass, exe, null);
    }

    protected Long insertSeed(SQLExecutor exe, Class<? extends Entity> aClass, int maxCount) throws SQLException{
        if (maxCount <= 0) maxCount = 100;
        Random rand = new Random();
        Long startTimestamp = 0l;

        List<Row> batch = new ArrayList<>();
        for (int count = 0; count < maxCount; ++count) {
            Passenger passenger = new Passenger();
            passenger.setName("Name_" + count);
            passenger.setAge(20 + count);
            passenger.setSex(rand.nextInt(2) == 1 ? "MALE" : "FEMALE");
            passenger.setDob(new java.sql.Date(Instant.now().toEpochMilli()));
            passenger.setCreatedate(new java.sql.Timestamp(Instant.now().toEpochMilli()));
            //Insert
            batch.add(passenger.getRow());
            if (startTimestamp == 0l) startTimestamp = passenger.getCreatedate().getTime();
        }
        //Insert In Batch:
        Entity.insert(aClass, exe, 20, batch);
        //RowCount Test:
        int rows = Entity.count(aClass, exe, null);
        System.out.println("Total RowCount: " + rows);
        Assert.assertTrue(rows > 0);
        return startTimestamp;
    }

}
