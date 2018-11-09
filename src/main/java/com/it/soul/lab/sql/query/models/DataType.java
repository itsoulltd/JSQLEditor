package com.it.soul.lab.sql.query.models;

import java.sql.Blob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public enum DataType {
	INT,
	FLOAT,
	DOUBLE,
	BOOL,
	STRING,
	SQLDATE,
	SQLTIMESTAMP,
	BLOB,
	BYTEARRAY,
	OBJECT;
	
	public static DataType getDataType(Object value) {
		if(value instanceof Integer) {
			return DataType.INT;
		}else if(value instanceof Double) {
			return DataType.DOUBLE;
		}else if(value instanceof Float) {
			return DataType.FLOAT;
		}else if(value instanceof Boolean) {
			return DataType.BOOL;
		}else if(value instanceof String) {
			return DataType.STRING;
		}else if(value instanceof Date || value instanceof java.util.Date) {
			return DataType.SQLDATE;
		}else if(value instanceof Timestamp || value instanceof Time) {
			return DataType.SQLTIMESTAMP;
		}else if(value instanceof Blob) {
			return DataType.BLOB;
		}else if(value instanceof Byte[]) {
			return DataType.BYTEARRAY;
		}else {
			return DataType.OBJECT;
		}
	}
}
