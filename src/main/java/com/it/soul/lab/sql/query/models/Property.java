package com.it.soul.lab.sql.query.models;

import java.text.SimpleDateFormat;

public class Property {
	
	public Property() {
		super();
	}

	public static final String SQL_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private String key = null;
    private Object value = null;
    private DataType type = DataType.NULL_OBJECT;

	private Property(Object value){
		this.value = value;
		this.type = DataType.getDataType(value);
	}

	public Property(String key, Object value){
		this(value);
		this.key = key;
	}
	
	public Property(String key){
		this(key, null);
	}
	
    public Property(String key, Object value, DataType type){
        this.key = key;
        this.value = value;
        this.type = (type == null) ? DataType.getDataType(value) : type;
    }

    public Property(Property prop) {
        this(prop.getKey(), prop.getValue(), prop.getType());
    }

	@Override
	public boolean equals(Object obj) {
		
		if(obj instanceof Property){
			boolean isSame = false;
			Property compareble = (Property)obj;
			if(this.getKey() == compareble.getKey()){
				isSame = true;
			}
			return isSame;
		}else{
			return false;
		}
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String property) {
		this.key = property;
	}
	public DataType getType() {
		return type;
	}
	public void setType(DataType type) {
		this.type = type;
	}
	public void updateType() {
		this.type = DataType.getDataType(getValue());
	}

	private String getDateString(Object date) {
		String result = null;
		SimpleDateFormat formatter = new SimpleDateFormat(SQL_DATETIME_FORMAT);
		try {
			if (date != null 
					&& ((date instanceof java.util.Date) 
							|| (date instanceof java.sql.Date))) {

				result = formatter.format(date);
			}
		} catch (Exception ex) {
			result = null;
			ex.printStackTrace();
		}
		return result;
	}
	@Override
	public String toString() {
		String value = (getValue() != null) ? getValue().toString() : null;
		if (getValue() != null
                && getType() == DataType.SQLDATE)
		    value = getDateString(getValue());
		//
		return String.format("{\"key\":\"%s\",\"value\":\"%s\",\"type\":\"%s\"}"
                                , getKey(), value, getType().name());
	}
}
