package com.it.soul.lab.jpql.entity;

import com.it.soul.lab.sql.entity.PrimaryKey;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "jp_table")
@Table(name = "jp_table")
public class JPerson extends JPQLEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @PrimaryKey(name="uuid")
    //@Column(length = 250)
    private long uuid;

    private String name;
    private Integer age;

    @Column(nullable = true)
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean active;

    private Timestamp createDate;
    private Timestamp modifyDate;

    public long getUuid() {
        return uuid;
    }

    public void setUuid(long uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public Timestamp getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Timestamp modifyDate) {
        this.modifyDate = modifyDate;
    }
}
