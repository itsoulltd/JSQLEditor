package com.it.soul.lab.cql;

import com.it.soul.lab.cql.query.CQLQuery;
import com.it.soul.lab.cql.query.ReplicationStrategy;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLScalarQuery;

import java.sql.SQLException;

public class CQLExecutorRunner {

    public static void main(String[] args) throws SQLException, InterruptedException {
        CQLExecutor cqlExecutor = new CQLExecutor.Builder()
                .connectTo(9042, "127.0.0.1")
                .build();

        Boolean newKeyspace = cqlExecutor.createKeyspace("OrderTracker", ReplicationStrategy.SimpleStrategy, 1);
        if (newKeyspace){
            cqlExecutor.switchKeyspace("OrderTracker");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                cqlExecutor.close();
            } catch (Exception e) {}
            System.out.println("Shutdown gracefully");
        }));
        int ticker = 0;
        while (ticker < 10){
            //
            SQLScalarQuery countQuery = new CQLQuery.Builder(QueryType.COUNT).columns().on(OrderEvent.class).build();
            int rows = cqlExecutor.getScalarValue(countQuery);
            System.out.println("Total RowCount: " + rows);
            //
            Thread.sleep(60000); // 60s
            ticker++;
        }
        System.exit(0);//Ending Run
    }

}
