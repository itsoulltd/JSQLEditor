package com.it.soul.lab.sql.query.builder;

public interface IndexBuilder extends QueryBuilder {
    ColumnsBuilder index(String name);
    ColumnsBuilder uniqueIndex(String name);
}
