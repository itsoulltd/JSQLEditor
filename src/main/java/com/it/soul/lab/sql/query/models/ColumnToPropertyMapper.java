package com.it.soul.lab.sql.query.models;

import com.it.soul.lab.sql.entity.Entity;

import java.util.Map;

@FunctionalInterface
public interface ColumnToPropertyMapper {
    Map<String, String> mapColumnsToProperties(Class<? extends Entity> entityType);
}
