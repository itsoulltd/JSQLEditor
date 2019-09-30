package com.it.soul.lab.sql.query.models;

import java.sql.Blob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public enum DataType {
	INT,
	LONG,
	FLOAT,
	DOUBLE,
	BOOL,
	STRING,
	TEXT,
	SQLDATE,
	SQLTIMESTAMP,
	BLOB,
	BYTEARRAY,
	OBJECT,
	UUID,
	LIST,
    MAP,
    NULL_OBJECT,
    NULL_SKIP,
	JSON;
	
	public static DataType getDataType(Object value) {
		if (value == null) return DataType.NULL_OBJECT;
		if(value instanceof Integer) {
			return DataType.INT;
		}else if(value instanceof Long) {
            return DataType.LONG;
        }else if(value instanceof Double) {
			return DataType.DOUBLE;
		}else if(value instanceof Float) {
			return DataType.FLOAT;
		}else if(value instanceof Boolean) {
			return DataType.BOOL;
		}else if(value instanceof String) {
		    return checkJsonType((String) value);
		}else if(value instanceof Date || value instanceof java.util.Date) {
			return DataType.SQLDATE;
		}else if(value instanceof Timestamp || value instanceof Time) {
			return DataType.SQLTIMESTAMP;
		}else if(value instanceof Blob) {
			return DataType.BLOB;
		}else if(value instanceof Byte[]) {
			return DataType.BYTEARRAY;
		}else if(value instanceof UUID) {
			return DataType.UUID;
		}else if (value instanceof List) {
		    return DataType.LIST;
        }else if (value instanceof Map) {
            return DataType.MAP;
        }else {
			return DataType.OBJECT;
		}
	}

	private static DataType checkJsonType(String value){
        if ((value).startsWith("{")
                || (value).startsWith("[")){
            return DataType.JSON;
        }
        return DataType.STRING;
    }
}
