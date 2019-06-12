package com.it.soul.lab.cql;

import com.it.soul.lab.cql.entity.CQLEntity;
import com.it.soul.lab.cql.entity.ClusteringKey;
import com.it.soul.lab.sql.entity.PrimaryKey;
import com.it.soul.lab.sql.entity.TableName;
import com.it.soul.lab.sql.query.models.DataType;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@TableName(value = "tracking_event")
public class OrderEvent extends CQLEntity {

    @PrimaryKey(name = "track_id")
    private String trackID; //Partitioning ID
    @PrimaryKey(name = "user_id")
    private String userID; //Partitioning ID

    @ClusteringKey(name = "uuid", type = DataType.UUID)
    private UUID uuid; //Clustering ID

    private Date timestamp = new Date();
    private Map<String, String> kvm;
    private Map<String, Integer> kvm2;

    public OrderEvent() {}

    public Map<String, String> getKvm() {
        return kvm;
    }

    public void setKvm(Map<String, String> kvm) {
        this.kvm = kvm;
    }

    public String getTrackID() {
        return trackID;
    }

    public void setTrackID(String trackID) {
        this.trackID = trackID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Integer> getKvm2() {
        return kvm2;
    }

    public void setKvm2(Map<String, Integer> kvm2) {
        this.kvm2 = kvm2;
    }

}
