package com.it.soul.lab.cql;

import com.it.soul.lab.cql.query.CQLQuery;
import com.it.soul.lab.cql.query.CQLSelectQuery;
import com.it.soul.lab.cql.query.ReplicationStrategy;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLScalarQuery;
import com.it.soul.lab.sql.query.models.Operator;
import com.it.soul.lab.sql.query.models.Predicate;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.Where;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

public class CQLExecutorAsyncReadTest {

    CQLExecutor cqlExecutor;

    @Before
    public void before() throws SQLException {
        //
        cqlExecutor = new CQLExecutor.Builder()
                .connectTo(9042, "127.0.0.1")
                .build();

        Boolean newKeyspace = cqlExecutor.createKeyspace("OrderTracker", ReplicationStrategy.SimpleStrategy, 1);
        if (newKeyspace){
            cqlExecutor.switchKeyspace("OrderTracker");
        }
        //
    }

    @After
    public void after(){
        //
        try {
            cqlExecutor.close();
        } catch (Exception e) {}
    }

    private Long generateSeedOrderEvent(int limit, UUID clusterUUID) throws SQLException {
        if (limit <= 0) limit = 100;
        //Creating a Table from CQLEntity @TableName description.
        boolean isDropped = cqlExecutor.dropTable(OrderEvent.class);
        if (isDropped) {
            boolean created = cqlExecutor.createTable(OrderEvent.class);
            Assert.assertTrue("Successfully Created", created);
        }
        //
        Long startTimestamp = 0l;
        for (int count = 0; count < limit; ++count) {
            OrderEvent event = new OrderEvent();
            event.setTrackID("my-device-tracker-id");
            event.setUserID("towhid@gmail.com");
            event.setUuid(clusterUUID);
            event.setGuid("wh0rbu49qh61");

            Map<String, String> names = new HashMap<>();
            names.put("fname-" + count, "James");
            names.put("lname-" + count, "Julian");
            event.setKvm(names);

            Map<String, Integer> collections = new HashMap<>();
            collections.put("age-" + count, 1);
            collections.put("days-" + count, 24);
            event.setKvm2(collections);
            //Insert
            boolean inserted = event.insert(cqlExecutor);
            if (startTimestamp == 0l) startTimestamp = event.getTimestamp();
            Assert.assertTrue("Successfully Inserted", inserted);
        }

        //RowCount Test:
        SQLScalarQuery countQuery = new CQLQuery.Builder(QueryType.COUNT)
                .columns()
                .on(OrderEvent.class)
                .build();
        int rows = cqlExecutor.getScalarValue(countQuery);
        System.out.println("Total RowCount: " + rows);
        Assert.assertTrue(rows > 0);
        return startTimestamp;
    }

    @Test
    public void readWithWhereTest() throws Exception {
        //Prepare Seed-Data:
        int limit = 15;
        UUID clusterUUID = UUID.randomUUID();
        System.out.println(clusterUUID.toString());
        Long startTime = generateSeedOrderEvent(50, clusterUUID);
        Long delayedTimeByMillis = Duration.ofMillis(startTime).plusMillis(50).toMillis();
        //DateFormatter:
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");
        //Where Clause:
        Predicate where = new Where("user_id").isEqualTo("towhid@gmail.com")
                .and("track_id").isEqualTo("my-device-tracker-id")
                .and("uuid").isEqualTo(clusterUUID)
                .and("guid").isEqualTo("wh0rbu49qh61")
                .and("timestamp").isGreaterThenOrEqual(delayedTimeByMillis);
        //
        CQLSelectQuery query = new CQLQuery.Builder(QueryType.SELECT)
                .columns()
                .from(OrderEvent.class)
                .where(where)
                .addLimit(limit, 0)
                .build();
        List<OrderEvent> otherItems2 = cqlExecutor.executeSelect(query, OrderEvent.class);
        Assert.assertTrue(otherItems2.size() <= limit);
        //Print Result:
        System.out.println("Start Time was: " + formatter.format(new Date(startTime)));
        System.out.println("Total Row Found: " + otherItems2.size());
        otherItems2.stream().forEach(event ->
                System.out.println("ASC Event:  "
                        + formatter.format(new Date(event.getTimestamp()))
                        + " " + event.marshallingToMap(true))
        );
    }

    @Test
    public void readWithPaginationTest() throws Exception {
        //Prepare Seed-Data:
        int numberOfPage = 3;
        int limit = 10;
        UUID clusterUUID = UUID.randomUUID();
        System.out.println(clusterUUID.toString());
        Long startTime = generateSeedOrderEvent(27, clusterUUID);
        Long delayedStartTime = Duration.ofMillis(startTime).plusMillis(120).toMillis();
        //DateFormatter:
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");
        //
        System.out.println("Start Time was: " + formatter.format(new Date(startTime)));
        for (int i = 1; i <= numberOfPage; i++) {
            CQLSelectQuery query = new CQLQuery.Builder(QueryType.SELECT)
                    .columns()
                    .from(OrderEvent.class)
                    .where(new Where("user_id").isEqualTo("towhid@gmail.com")
                            .and("track_id").isEqualTo("my-device-tracker-id")
                            .and("uuid").isEqualTo(clusterUUID)
                            .and("guid").isEqualTo("wh0rbu49qh61")
                            .and("timestamp").isGreaterThen(delayedStartTime))
                    .addLimit(limit, 0)
                    .build();
            List<OrderEvent> otherItems2 = cqlExecutor.executeSelect(query, OrderEvent.class);
            //Print Result:
            System.out.println("Total Row Found: " + otherItems2.size());
            otherItems2.stream().forEach(event ->
                    System.out.println("ASC Event:  "
                            + formatter.format(new Date(event.getTimestamp()))
                            + " " + event.marshallingToMap(true))
            );
            //Find the next timestamp for pagination: which will use in next fetch:
            if (otherItems2.isEmpty()) continue;
            OrderEvent lastItem = otherItems2.get(otherItems2.size() - 1);
            delayedStartTime = lastItem.getTimestamp();
        }// End of For
    }

    @Test
    public void asyncReadAllTest() throws Exception {
        //Prepare Seed-Data:
        UUID clusterUUID = UUID.randomUUID();
        System.out.println(clusterUUID.toString());
        Long startTime = generateSeedOrderEvent(0, clusterUUID);
        //DateFormatter:
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");
        //
        // Test Sequence:
        // (pageSize:10 - rowCount:30)    (pageSize:10 - rowCount:25)  (pageSize:5 - rowCount:6)
        // (pageSize:30 - rowCount:101)   (pageSize:30 - rowCount:100) (pageSize:30 - rowCount:99)
        // (pageSize:30 - rowCount:1030)  (pageSize:30 - rowCount:29)
        // (pageSize:1 - rowCount:-1)   (pageSize:1 - rowCount:0)  [Fetch single row from DB]
        // (pageSize:0 - rowCount:-1)   (pageSize:-1 - rowCount:-1) [Caution: Both will fetch all rows from DB]
        OrderEvent.read(OrderEvent.class, cqlExecutor
                , 1, 0
                , new Property("timestamp", startTime)
                , Operator.ASC
                , (nextKey) -> {
                    //Where Clause:
                    return new Where("user_id").isEqualTo("towhid@gmail.com")
                            .and("track_id").isEqualTo("my-device-tracker-id")
                            .and("uuid").isEqualTo(clusterUUID)
                            .and("guid").isEqualTo("wh0rbu49qh61")
                            .and(nextKey.getKey()).isGreaterThenOrEqual(nextKey.getValue());
                }
                , (orderEvents) -> {
                    //Print Result:
                    orderEvents.stream().forEach(event ->
                            System.out.println("Event:  "
                                    + formatter.format(new Date(event.getTimestamp()))
                                    + " " + event.marshallingToMap(true))
                    );
                    System.out.println("Row Count: " + orderEvents.size() + " \n");
                });
    }

}
