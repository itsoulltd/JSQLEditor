package com.it.soul.lab.cql;

import com.it.soul.lab.cql.entity.CQLEntity;
import com.it.soul.lab.cql.entity.CQLIndex;
import com.it.soul.lab.cql.entity.ClusteringKey;
import com.it.soul.lab.cql.entity.EnableTimeToLive;
import com.it.soul.lab.sql.entity.Column;
import com.it.soul.lab.sql.entity.Ignore;
import com.it.soul.lab.sql.entity.PrimaryKey;
import com.it.soul.lab.sql.entity.TableName;
import com.it.soul.lab.sql.query.models.DataType;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@TableName(value = "order_event")
@EnableTimeToLive(300L) //TimeToLive 60*5 sec = 5 min
//@EnableTimeToLive(2160000L) //TimeToLive 60*60*24*25 sec = 25 days
public class OrderEvent extends CQLEntity {

    @PrimaryKey(name = "track_id")
    private String trackID; //Partitioning ID

    @PrimaryKey(name = "user_id")
    private String userID; //Partitioning ID

    @ClusteringKey(name = "uuid", type = DataType.UUID)
    private UUID uuid; //Clustering ID

    @ClusteringKey(name = "guid")
    @CQLIndex(custom = true
            , using = "org.apache.cassandra.index.sasi.SASIIndex"
            , options = "'mode': 'CONTAINS', 'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer', 'case_sensitive': 'false'")
    private String guid; //Clustering ID

    private Date timestamp = new Date();
    private Map<String, String> kvm;
    private Map<String, Integer> kvm2;

    @Column(name = "last_entry_id")
    private String lastEntryId;

    @Ignore
    private static int _autoIncrement;

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

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getLastEntryId() {
        return lastEntryId;
    }

    public void setLastEntryId(String lastEntryId) {
        this.lastEntryId = lastEntryId;
    }
}
