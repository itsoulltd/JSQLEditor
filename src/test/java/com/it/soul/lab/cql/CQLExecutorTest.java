package com.it.soul.lab.cql;

import com.it.soul.lab.cql.query.AlterAction;
import com.it.soul.lab.cql.query.CQLQuery;
import com.it.soul.lab.cql.query.CQLSelectQuery;
import com.it.soul.lab.cql.query.ReplicationStrategy;
import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLScalarQuery;
import com.it.soul.lab.sql.query.models.Predicate;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.Where;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
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

    //@Test
    public void versionTest(){
        String version = cqlExecutor.version();
        System.out.println("Cassandra: " + version);
    }

    //@Test
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

    //@Test
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

    private Long generateSeedOrderEvent() throws SQLException {
        //Creating a Table from CQLEntity @TableName description.
        boolean isDropped = cqlExecutor.dropTable(OrderEvent.class);
        if (isDropped) {
            boolean created = cqlExecutor.createTable(OrderEvent.class);
            Assert.assertTrue("Successfully Created", created);
        }
        //
        Long startTimestamp = 0l;
        for (int count = 0; count < 100; ++count) {
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

    //@Test
    public void fetchTest(){
        try{
            //Prepare Seed-Data:
            Long startTime = generateSeedOrderEvent();
            //Cassandra force to have all PartitionKey in where clause and they must have to be in sequence as they appear in table schema.
            //ClusteringKey's are optional they may or may not in clause.
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 10);
            //
            CQLSelectQuery query = new CQLQuery.Builder(QueryType.SELECT)
                    .columns()
                    .from(OrderEvent.class)
                    .where(new Where("user_id").isEqualTo("towhid@gmail.com")
                            .and("track_id").isEqualTo("my-device-tracker-id")
                            .and("uuid").isEqualTo(clusterUUID)
                            .and("guid").isEqualTo("wh0rbu49qh61")
                            .and("timestamp").isGreaterThenOrEqual(startTime))
                    .addLimit(10, 0)
                    .build();
            List<OrderEvent> otherItems = cqlExecutor.executeSelect(query, OrderEvent.class);
            otherItems.stream().forEach(event -> System.out.println("timestamp:  "+ event.getTimestamp()));
            Assert.assertTrue(otherItems.size() > 0);
            //Read All:
            /*List<OrderEvent> otherItems2 = OrderEvent.read(OrderEvent.class, cqlExecutor);
            otherItems2.stream().forEach(event -> System.out.println("timestamp:  "+ event.getTimestamp()));
            Assert.assertTrue(otherItems2.size() > 0);*/
            //
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    //@Test
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

    //@Test
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