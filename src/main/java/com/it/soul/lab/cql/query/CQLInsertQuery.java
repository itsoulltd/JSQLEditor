package com.it.soul.lab.cql.query;

import com.it.soul.lab.sql.query.SQLInsertQuery;

import java.time.Duration;

public class CQLInsertQuery extends SQLInsertQuery {

    protected StringBuffer timeBuffer = new StringBuffer();

    @Override
    public String toString() {
        return super.toString() + " " + timeBuffer.toString();
    }

    public void usingTTL(long seconds) {

        //to prevent multiple call of this method.
        if (timeBuffer.length() > 0 && timeBuffer.toString().startsWith("USING TTL")) return;

        if (timeBuffer.length() <= 0)
            timeBuffer.append("USING TTL " + Duration.ofSeconds(seconds).getSeconds());
        else {
            String old = timeBuffer.toString().substring("USING TIMESTAMP ".length());
            timeBuffer = new StringBuffer();
            timeBuffer.append("USING TTL " + Duration.ofSeconds(seconds).getSeconds() + " AND TIMESTAMP " + old);
        }
    }

//    public void usingTimestamp(LocalDateTime timestamp){
//        if (timeBuffer.length() <= 0){
//            timeBuffer.append("USING TIMESTAMP " + timestamp.getNano() / 1000);
//        }else{
//            timeBuffer.append(" AND TIMESTAMP " + timestamp.getNano() / 1000);
//        }
//    }

}
