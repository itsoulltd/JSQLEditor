package com.it.soul.lab.sql.entity;

public enum ColumnConstraint {
    NONE(""),
    NOT_NULL("NOT NULL"),
    UNIQUE("UNIQUE"),
    PRIMARY_KEY("PRIMARY KEY"),
    FOREIGN_KEY("FOREIGN KEY"),
    CHECK("CHECK"),
    DEFAULT("DEFAULT"),
    INDEX("INDEX");

    private String value;
    ColumnConstraint(String value){
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
