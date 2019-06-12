package com.it.soul.lab.sql.query.models;

public class WhereProperties extends Row {

    @Override
    public Row add(Property prop){
        if (prop == null) {return this;}
        getProperties().add(prop);
        return this;
    }

}
