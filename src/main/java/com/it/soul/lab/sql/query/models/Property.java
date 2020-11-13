package com.it.soul.lab.sql.query.models;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Property implements Comparable<Property>, Externalizable {

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
			Property comparable = (Property)obj;
			if (getValue() == null || comparable.getValue() == null) return false;
			return Objects.equals(getValue(), comparable.getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getKey(), getValue());
	}

	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
		updateType();
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

	@Override
	public int compareTo(Property o) {
		if (o == null || getValue() == null) return -1;
		if (getType() != o.getType()) return -1;
		int result = -1;
		String value = getValue().toString();
		String oValue = o.getValue().toString();
		switch (getType()){
			case INT:
				result = Integer.valueOf(value).compareTo(Integer.valueOf(oValue));
				break;
			case LONG:
				result = Long.valueOf(value).compareTo(Long.valueOf(oValue));
				break;
			case FLOAT:
				result = Float.valueOf(value).compareTo(Float.valueOf(oValue));
				break;
			case BOOL:
				result = Boolean.valueOf(value).compareTo(Boolean.valueOf(oValue));
				break;
			case DOUBLE:
				result = Double.valueOf(value).compareTo(Double.valueOf(oValue));
				break;
			case BIG_DECIMAL:
				result = new BigDecimal(value).compareTo(new BigDecimal(oValue));
				break;
			case UUID:
				result = UUID.fromString(value).compareTo(UUID.fromString(oValue));
				break;
			case SQLDATE:
			case SQLTIMESTAMP:
				SimpleDateFormat format = new SimpleDateFormat(Property.SQL_DATETIME_FORMAT);
				try {
					result = format.parse(value).compareTo(format.parse(oValue));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				break;
			case STRING:
			case TEXT:
				result = value.compareTo(oValue);
		}
		return result;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		Map<String, Object> props = new HashMap<>();
		props.put("key", getKey());
		props.put("value", getValue());
		out.writeObject(props);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		Object object = in.readObject();
		if (object instanceof Map){
			Map<String, Object> data = (Map) object;
			setKey(data.get("key").toString());
			setValue(data.get("value"));
		}
	}
}
