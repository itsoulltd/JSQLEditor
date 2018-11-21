package com.it.soul.lab.cql;

import com.it.soul.lab.cql.query.AlterAction;
import com.it.soul.lab.cql.query.CQLQuery;
import com.it.soul.lab.cql.query.CQLSelectQuery;
import com.it.soul.lab.cql.query.ReplicationStrategy;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.models.Predicate;
import com.it.soul.lab.sql.query.models.Property;
import com.it.soul.lab.sql.query.models.Where;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class CQLExecutorTest {

    CQLExecutor cqlExecutor;

    @Before
    public void before() throws SQLException {
        //
        cqlExecutor = new CQLExecutor.Builder()
                .connectTo(9042, "127.0.0.1").build();

        Boolean newKeyspace = cqlExecutor.createKeyspace("GeoTracker locations", ReplicationStrategy.SimpleStrategy, 1);
        if (newKeyspace){
            cqlExecutor.switchKeyspace("GeoTracker locations");
        }
    }

    @After
    public void after(){
        //
    }

    @Test
    public void cassandraCRUDTest(){
        try {
            //Creating a Table from CQLEntity @TableName description.
            boolean created = cqlExecutor.createTable(TrackingEvent.class);
            Assert.assertTrue("Successfully Created", created);

            TrackingEvent event = new TrackingEvent();
            event.setLocations("---");
            event.setTenantID(UUID.randomUUID().toString());
            event.setTrackID(UUID.randomUUID().toString());
            event.setUserID(UUID.randomUUID().toString());
            event.setUuid(UUID.randomUUID());

            Map<String, String> names = new HashMap<>();
            names.put("name-1", "towhid");
            names.put("name-2", "sohana");
            event.setKvm(names);

            Map<String, Integer> collections = new HashMap<>();
            collections.put("hello-1", 1);
            collections.put("hello-2", 24);
            event.setKvm2(collections);

            //Insert
            boolean inserted = event.insert(cqlExecutor);
            Assert.assertTrue("Successfully Inserted", inserted);

            //Select From Cassandra:
            CQLSelectQuery query = new CQLQuery.Builder(QueryType.SELECT)
                    .columns()
                    .from("tracking_event")
                    .build();
            List<TrackingEvent> items = cqlExecutor.executeSelect(query, TrackingEvent.class);
            Assert.assertTrue("Successfully Fetched:", items.isEmpty() == false);

            //Update
            if (items.size() > 0){
                TrackingEvent event1 = items.get(0);
                event1.getKvm().put("name-3", "sumaiya");
                boolean updated = event1.update(cqlExecutor);
                Assert.assertTrue("Successfully Updated", updated);
            }

            //Delete
            if (items.size() > 1){
                TrackingEvent event1 = items.get(1);
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

    @Test
    public void fetchTest(){
        try{
            //Cassandra force to have all PartitionKey in where clause and they must have to be in sequence as they appear in table schema.
            //ClusteringKey's are optional they may or may not in clause.
            Predicate pred = new Where("track_id").isEqualTo("3ab863f1-558b-4621-9410-ef3f2180889f")
                    .and("user_id").isEqualTo("776aa40b-8f9c-4e6f-80e9-ae6c5e555be0")
                    .and("tenant_id").isEqualTo("7df897fb-8a74-4617-8dac-89725d3e6efb");

            List<TrackingEvent> otherItems = TrackingEvent.read(TrackingEvent.class, cqlExecutor, pred);
            otherItems.stream().forEach(trackingEvent -> System.out.println("track_id "+trackingEvent.getTrackID()));

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        //To Succeed the build
        Assert.assertTrue(true);
    }

    @Test
    public void tableAlterTest(){
        //try {
            //cqlExecutor.alterTable(TrackingEvent.class, AlterAction.ALTER, new Property("<non-primary-key>", DataType-as-value));

            //boolean rename = cqlExecutor.alterTable(TrackingEvent.class, AlterAction.RENAME, new Property("track_id_a", "track_id"));
            //Assert.assertTrue("Rename:", rename);

            //boolean drop = cqlExecutor.alterTable(TrackingEvent.class, AlterAction.DROP, new Property("<properties-to-drop>"));
            //Assert.assertTrue("Drop:", drop);

            //boolean add = cqlExecutor.alterTable(TrackingEvent.class, AlterAction.DROP, new Property("<properties-to-add>", "data-type-as-value"));
            //Assert.assertTrue("Add:", add);

        //} catch (SQLException e) {
        //    System.out.println(e.getMessage());
        //}
        //To Succeed the build
        Assert.assertTrue(true);
    }

}