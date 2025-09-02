package com.it.soul.lab.jpql.entity;

import com.it.soul.lab.sql.entity.PrimaryKey;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class JPassenger extends com.it.soul.lab.sql.entity.Entity {
    @Id
    @PrimaryKey(name="uuid")
    @Column(length = 250)
    private String uuid;

    private String name;
    private Integer age;
    private String sex;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
