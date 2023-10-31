package com.it.soul.lab.cql;

import com.it.soul.lab.cql.query.AlterAction;
import com.it.soul.lab.cql.query.CQLQuery;
import com.it.soul.lab.cql.query.CQLSelectQuery;
import com.it.soul.lab.cql.query.ReplicationStrategy;
import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLScalarQuery;
import com.it.soul.lab.sql.query.builder.WhereExpressionBuilder;
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

public class CQLExecutorTest {

    CQLExecutor cqlExecutor;
    UUID clusterUUID;

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
        clusterUUID = UUID.randomUUID();
        System.out.println(clusterUUID.toString());
    }

    @After
    public void after(){
        //
        try {
            cqlExecutor.close();
        } catch (Exception e) {}
    }

    @Test
    public void versionTest(){
        String version = cqlExecutor.version();
        System.out.println("Cassandra: " + version);
    }

    @Test
    public void cassandraInsertTest() {
        try {
            //Creating a Table from CQLEntity @TableName description.
            boolean isDropped = cqlExecutor.dropTable(OrderEvent.class);
            if (isDropped) {
                boolean created = cqlExecutor.createTable(OrderEvent.class);
                Assert.assertTrue("Successfully Created", created);
                if (created){
                    //If alter needed:
                    try {
                        boolean add = cqlExecutor.alterTable(OrderEvent.class, AlterAction.ADD, new Property("last_entry_id", ""));
                        Assert.assertTrue("Add:", add);
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            //
            OrderEvent event = new OrderEvent();
            event.setTrackID("my-device-tracker-id");
            event.setUserID("towhid@gmail.com");
            event.setUuid(clusterUUID);
            event.setGuid("wh0rbu49qh61");

            Map<String, String> names = new HashMap<>();
            names.put("name-1", "James");
            names.put("name-2", "Julian");
            event.setKvm(names);

            Map<String, Integer> collections = new HashMap<>();
            collections.put("hello-1", 1);
            collections.put("hello-2", 24);
            event.setKvm2(collections);

            //Insert
            boolean inserted = event.insert(cqlExecutor);
            Assert.assertTrue("Successfully Inserted", inserted);

            //RowCount Test:
            SQLScalarQuery countQuery = new CQLQuery.Builder(QueryType.COUNT).columns().on(OrderEvent.class).build();
            int rows = cqlExecutor.getScalarValue(countQuery);
            System.out.println("Total RowCount: " + rows);
            Assert.assertTrue(rows > 0);

            //Select From Cassandra:
            CQLSelectQuery query = new CQLQuery.Builder(QueryType.SELECT)
                    .columns()
                    .from(OrderEvent.class)
                    //.addLimit(10, 0) //not supported
                    .build();

            List<OrderEvent> items = cqlExecutor.executeSelect(query, OrderEvent.class);
            Assert.assertTrue("Successfully Fetched:", items.isEmpty() == false);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
        } catch (InstantiationException e) {
            System.out.println(e.getMessage());
        }
        //To Succeed the build
        Assert.assertTrue(true);
    }

    @Test
    public void cassandraCRUDTest(){
        try {
            //Creating a Table from CQLEntity @TableName description.
            boolean created = cqlExecutor.createTable(OrderEvent.class);
            Assert.assertTrue("Successfully Created", created);

            OrderEvent event = new OrderEvent();
            event.setTrackID(UUID.randomUUID().toString());
            event.setUserID(UUID.randomUUID().toString());
            event.setUuid(UUID.randomUUID());
            event.setGuid("wh0rbu49qh61");

            Map<String, String> names = new HashMap<>();
            names.put("name-1", "James");
            names.put("name-2", "Julian");
            event.setKvm(names);

            Map<String, Integer> collections = new HashMap<>();
            collections.put("hello-1", 1);
            collections.put("hello-2", 24);
            event.setKvm2(collections);

            //Insert
            boolean inserted = event.insert(cqlExecutor);
            Assert.assertTrue("Successfully Inserted", inserted);

            //RowCount Test:
            SQLScalarQuery countQuery = new CQLQuery.Builder(QueryType.COUNT).columns().on(OrderEvent.class).build();
            int rows = cqlExecutor.getScalarValue(countQuery);
            System.out.println("Total RowCount: " + rows);
            Assert.assertTrue(rows > 0);

            //Select From Cassandra:
            CQLSelectQuery query = new CQLQuery.Builder(QueryType.SELECT)
                                                .columns()
                                                .from(OrderEvent.class)
                                                //.addLimit(10, 0) //not supported
                                                .build();

            List<OrderEvent> items = cqlExecutor.executeSelect(query, OrderEvent.class);
            Assert.assertTrue("Successfully Fetched:", items.isEmpty() == false);

            //Update
            if (items.size() > 0){
                OrderEvent event1 = items.get(0);
                event1.getKvm().put("name-3", "Weber");
                boolean updated = event1.update(cqlExecutor);
                Assert.assertTrue("Successfully Updated", updated);
            }

            //Delete
            if (items.size() > 1){
                OrderEvent event1 = items.get(1);
                boolean delete = event1.delete(cqlExecutor);
                Assert.assertTrue("Successfully Deleted", delete);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
        } catch (InstantiationException e) {
            System.out.println(e.getMessage());
        }
        //To Succeed the build
        Assert.assertTrue(true);
    }

    private Long generateSeedOrderEvent(int limit) throws SQLException {
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
    public void fetchTest(){
        try{
            //Prepare Seed-Data:
            Long startTime = generateSeedOrderEvent(25);
            //DateFormatter:
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");

            //Cassandra force to have all PartitionKey in where clause, and they must have to be in sequence as they appear in table schema.
            //ClusteringKey's are optional they may or may not in clause.
            //Where Clause:
            Predicate clause = new Where("user_id").isEqualTo("towhid@gmail.com")
                    .and("track_id").isEqualTo("my-device-tracker-id")
                    .and("uuid").isEqualTo(clusterUUID)
                    .and("guid").isEqualTo("wh0rbu49qh61")
                    .and("timestamp").isGreaterThenOrEqual(startTime);

            //SearchQuery DESC:
            CQLSelectQuery query_desc = new CQLQuery.Builder(QueryType.SELECT)
                    .columns()
                    .from(OrderEvent.class)
                    .where(clause)
                    .orderBy(Operator.DESC, "timestamp")
                    .addLimit(10, 0)
                    .build();
            List<OrderEvent> otherItems = cqlExecutor.executeSelect(query_desc, OrderEvent.class);
            //Print Result:
            otherItems.stream().forEach(event ->
                    System.out.println("DESC Event:  "
                            + formatter.format(new Date(event.getTimestamp()))
                            + " " + event.marshallingToMap(true))
            );
            Assert.assertTrue(otherItems.size() > 0);
            System.out.println("\n");

            //Search Asc:
            CQLSelectQuery query_asc = new CQLQuery.Builder(QueryType.SELECT)
                    .columns()
                    .from(OrderEvent.class)
                    .where(clause)
                    .orderBy(Operator.ASC, "timestamp")
                    .addLimit(10, 0)
                    .build();
            //
            //List<OrderEvent> otherItems2 = OrderEvent.read(OrderEvent.class, cqlExecutor, clause);
            List<OrderEvent> otherItems2 = cqlExecutor.executeSelect(query_asc, OrderEvent.class);
            //Print Result:
            otherItems2.stream().forEach(event ->
                    System.out.println("ASC Event:  "
                            + formatter.format(new Date(event.getTimestamp()))
                            + " " + event.marshallingToMap(true))
            );
            Assert.assertTrue(otherItems2.size() > 0);
            //
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void readAllTest() throws Exception {
        //Prepare Seed-Data:
        Long startTime = generateSeedOrderEvent(20);
        //DateFormatter:
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS a");
        //
        List<OrderEvent> otherItems2 = OrderEvent.read(OrderEvent.class, cqlExecutor);
        Assert.assertTrue(otherItems2.size() == 20);
        //Print Result:
        System.out.println("Start Time was: " + formatter.format(new Date(startTime)));
        otherItems2.stream().forEach(event ->
                System.out.println("ASC Event:  "
                        + formatter.format(new Date(event.getTimestamp()))
                        + " " + event.marshallingToMap(true))
        );
    }

    @Test
    public void readWithWhereTest() throws Exception {
        //Prepare Seed-Data:
        int limit = 15;
        Long startTime = generateSeedOrderEvent(50);
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
        Long startTime = generateSeedOrderEvent(27);
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
        Long startTime = generateSeedOrderEvent(0);
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

    @Test
    public void tableAlterTest() throws SQLException {
        //boolean alter = cqlExecutor.alterTable(OrderEvent.class, AlterAction.ALTER, new Property("<non-primary-key>", "data-type-as-value"));
        //Assert.assertTrue("Alter:", alter);

        //boolean rename = cqlExecutor.alterTable(OrderEvent.class, AlterAction.RENAME, new Property("<properties-to-rename>", "<properties-new-name>"));
        //Assert.assertTrue("Rename:", rename);

        //boolean drop = cqlExecutor.alterTable(OrderEvent.class, AlterAction.DROP, new Property("<properties-to-drop>"));
        //e.g.
        boolean drop = cqlExecutor.alterTable(OrderEvent.class, AlterAction.DROP, new Property("myNewProp"));
        Assert.assertTrue("Drop:", drop);

        //boolean add = cqlExecutor.alterTable(OrderEvent.class, AlterAction.ADD, new Property("<properties-to-add>", "data-type-as-value"));
        //e.g.
        //boolean add = cqlExecutor.alterTable(OrderEvent.class, AlterAction.ADD, new Property("myNewProp", "myValType"));
        //Assert.assertTrue("Add:", add);

        //To Succeed the build
        Assert.assertTrue(true);
    }

    @Test
    public void indexTest(){
        try {
            boolean droped = cqlExecutor.dropTable(OrderEvent.class);
            boolean created = cqlExecutor.createTable(OrderEvent.class);
            boolean isDone = cqlExecutor.createIndexOn("guid", OrderEvent.class);

            addEvent("Uttara-10", "wh0rbu49qh61");
            addEvent("Dhaka-City", "wh0qcrdbngk4");
            addEvent("Fhulbaria", "wh0rbeq1t329");
            addEvent("Uttara-3", "wh0rc02u9d88");
            addEvent("BamnarTek", "wh0rbshf6x0m");
            addEvent("Uttara-4", "wh0rc0m8j2s6");
            addEvent("Abdullahpur", "wh0rchf3uw1x");
            addEvent("KamarPara", "wh0rbsy8v1wt");

            printAll();
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            List<OrderEvent> items = search("wh0rbu49qh61", 5, cqlExecutor);
            items.forEach(orderEvent -> System.out.println(orderEvent.getUserID() + "# " + orderEvent.getGuid()));

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    private boolean addEvent(String city, String guid) throws SQLException {
        OrderEvent event = new OrderEvent();
        event.setTrackID(UUID.randomUUID().toString());
        event.setUserID(city);
        event.setUuid(UUID.randomUUID());
        event.setGuid(guid);
        return event.insert(cqlExecutor);
    }

    private List<OrderEvent> search(String guid, int matchUptoLength, CQLExecutor executor) throws IllegalAccessException, InstantiationException, SQLException {
        //
        if (guid == null || guid.isEmpty()) return new ArrayList<>();
        if (matchUptoLength <= 0) matchUptoLength = 5;
        String likeToBe = "%" + guid.substring(0, matchUptoLength) + "%";
        //
        String tableName = com.it.soul.lab.sql.entity.Entity.tableName(OrderEvent.class);
        CQLSelectQuery nearby = new CQLQuery.Builder(QueryType.SELECT)
                .columns()
                .from(tableName)
                .where(new Where("guid").isLike(likeToBe))
                .build();
        //
        List<OrderEvent> items = executor.executeSelect(nearby, OrderEvent.class);
        return items;
    }

    private void printAll() throws IllegalAccessException, SQLException, InstantiationException {
        //
        CQLSelectQuery query = new CQLQuery.Builder(QueryType.SELECT)
                .columns()
                .from(Entity.tableName(OrderEvent.class))
                .build();
        //
        List<OrderEvent> items = cqlExecutor.executeSelect(query, OrderEvent.class);
        items.forEach(orderEvent -> System.out.println(orderEvent.getUserID() + "# " + orderEvent.getGuid()));
    }

}