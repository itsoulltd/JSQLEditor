package com.it.soul.lab.sql.query.models;

public enum DDLType {

    INDEX("INDEX"),
    UNIQUE_INDEX("UNIQUE INDEX"),
    DATABASE("DATABASE"),
    TABLE("TABLE"),
    CONSTRAINTS("constraint"),
    PRIMARY_KEY("PRIMARY KEY"),
    FOREIGN_KEY("FOREIGN KEY"),
    NOT_NULL("NOT NULL"),
    CHECK("CHECK"),
    DEFAULT("DEFAULT"),
    AUTO_INCREMENT("AUTO INCREMENT"),
    DATES("DATES"),
    VIEWS("VIEWS"),
    INJUNCTION("INJUNCTION"),
    HOSTING("HOSTING"),
    UNIQUE("UNIQUE");

    private String val;

    DDLType(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }
}
