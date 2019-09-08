package com.it.soul.lab.sql.query.builder;

import com.it.soul.lab.sql.query.models.Property;

public interface InsertBuilder extends QueryBuilder{
	QueryBuilder values(Property...properties);
}