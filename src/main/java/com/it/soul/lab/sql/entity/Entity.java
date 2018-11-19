package com.it.soul.lab.sql.entity;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.it.soul.lab.sql.QueryExecutor;
import com.it.soul.lab.sql.query.SQLDeleteQuery;
import com.it.soul.lab.sql.query.SQLInsertQuery;
import com.it.soul.lab.sql.query.SQLQuery;
import com.it.soul.lab.sql.query.SQLQuery.QueryType;
import com.it.soul.lab.sql.query.SQLSelectQuery;
import com.it.soul.lab.sql.query.SQLUpdateQuery;
import com.it.soul.lab.sql.query.models.AndExpression;
import com.it.soul.lab.sql.query.models.DataType;
import com.it.soul.lab.sql.query.models.Expression;
import com.it.soul.lab.sql.query.models.ExpressionInterpreter;
import com.it.soul.lab.sql.query.models.Operator;
import com.it.soul.lab.sql.query.models.Property;

public abstract class Entity implements EntityInterface{
	public Entity() {
		super();
	}
	protected boolean hasColumnAnnotation(Field field) {
		boolean isAnnotated = field.isAnnotationPresent(Column.class)
				|| field.isAnnotationPresent(PrimaryKey.class);
		return isAnnotated;
	}
	protected List<Property> getProperties(QueryExecutor exe, boolean skipPrimary) {
		List<Property> result = new ArrayList<>();
		boolean acceptAll = shouldAcceptAllProperty();
		for (Field field : this.getClass().getDeclaredFields()) {
			if(acceptAll == false && hasColumnAnnotation(field) == false) {
				continue;
			}
			Property prop = getProperty(field.getName(), exe, skipPrimary);
			if(prop == null) {continue;}
			result.add(prop);
		}
		return result;
	}
	private DataType getDataType(Object value) {
		return DataType.getDataType(value);
	}
	private java.util.Date parseDate(String val, DataType type, String format){
		try {
			SimpleDateFormat formatter = new SimpleDateFormat((format != null && format.trim().isEmpty() == false) 
																		? format 
																		: Property.SQL_DATETIME_FORMAT);
			java.util.Date date = formatter.parse(val);
			if(type == DataType.SQLTIMESTAMP) {
				return new Timestamp(date.getTime());
			}else {
				return new Date(date.getTime());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	private Object getFieldValue(Field field, QueryExecutor exe) throws IllegalArgumentException, IllegalAccessException, SQLException {
		Object value = field.get(this);
		//
		if(value == null && field.isAnnotationPresent(Column.class) == true) {
			Column annotation = field.getAnnotation(Column.class);
			String defaultVal = annotation.defaultValue();
			DataType type = annotation.type();
			switch (type) {
			case INT:
				value = Integer.valueOf(defaultVal);
				break;
			case FLOAT:
				value = Float.valueOf(defaultVal);
				break;
			case DOUBLE:
				value = Double.valueOf(defaultVal);
				break;
			case BOOL:
				value = Boolean.valueOf(defaultVal);
				break;
			case SQLDATE:
			case SQLTIMESTAMP:
				value = parseDate(defaultVal, type, annotation.parseFormat());
				break;
			case BLOB:
				value = (exe != null) ? exe.createBlob(defaultVal) : defaultVal;
				break;
			case BYTEARRAY:
				value = defaultVal.getBytes();
				break;
			default:
				value = defaultVal;
				break;
			}
		}
		//always.
		return value;
	}
	protected Property getProperty(String key, QueryExecutor exe, boolean skipPrimary) {
		Property result = null;
		try {
			Field field = this.getClass().getDeclaredField(key);
			if(field.isAnnotationPresent(PrimaryKey.class)) {
				if (skipPrimary) {return null;}
				if (((PrimaryKey)field.getAnnotation(PrimaryKey.class)).autoIncrement() == true
						 && skipPrimary != false) {return null;}
			}
			field.setAccessible(true);
			String actualKey = getPropertyKey(field);
			Object value = getFieldValue(field, exe);
			DataType type = getDataType(value);
			result = new Property(actualKey, value, type);
			field.setAccessible(false);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	protected String getPropertyKey(Field field) {
		//Introduce Column:name() -> So that, if we want to mapping different column naming in Database Schema.
		//Logic: if column annotation not present OR Column:Name() is empty, then return field.getName() 
		//       else return Column:name()
		if(field.isAnnotationPresent(Column.class) == false) {
			return field.getName();
		}
		Column column = field.getAnnotation(Column.class);
		boolean hasValue = column.name().trim().isEmpty() == false;
		return (hasValue) ? column.name().trim() : field.getName();
	}
	protected boolean shouldAcceptAllProperty() {
		if(this.getClass().isAnnotationPresent(TableName.class) == false) {
			return true;
		}
		TableName tableName = (TableName) this.getClass().getAnnotation(TableName.class);
		return tableName.acceptAll();
	}
	private Boolean _isAutoIncremented = null;
	private boolean isAutoIncrement() {
		if(_isAutoIncremented == null) {
			PrimaryKey primAnno = getPrimaryKey(); 
			if(primAnno == null) {
				_isAutoIncremented = false;
			}
			_isAutoIncremented = primAnno.autoIncrement();
		}
		return _isAutoIncremented;
	}
	protected PrimaryKey getPrimaryKey() {
		PrimaryKey key = null;
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			if(field.isAnnotationPresent(PrimaryKey.class)) {
				key = field.getAnnotation(PrimaryKey.class);
				break;
			}
		}
		return key;
	}
	protected List<PrimaryKey> getAllPrimaryKey() {
		List<PrimaryKey> keys = new ArrayList<>();
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			if(field.isAnnotationPresent(PrimaryKey.class)) {
				keys.add(field.getAnnotation(PrimaryKey.class));
			}
		}
		return keys;
	}
	protected Property getPrimaryProperty(QueryExecutor exe) {
		Property result = null;
		try {
			String key = getPrimaryKey().name().trim();
			result = getProperty(key, exe, false);
		} catch (SecurityException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return result;
	}
	protected List<Property> getAllPrimaryProperty(QueryExecutor exe) {
		List<Property> results = new ArrayList<>();
		try {
			for (PrimaryKey pmKey : getAllPrimaryKey()){
				String key = pmKey.name().trim();
				results.add(getProperty(key, exe, false));
			}
		} catch (SecurityException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return results;
	}
	public Boolean update(QueryExecutor exe, String...keys) throws SQLException, Exception {
		List<Property> properties = new ArrayList<>();
		if(keys.length > 0) {
			for (String key : keys) {
				String skey = key.trim();
				Property prop = getProperty(skey, exe, true);
				if (prop == null) {continue;}
				properties.add(prop);
			}
		}else {
			properties = getProperties(exe, true);
		}
		String tableName = Entity.tableName(getClass());
		SQLUpdateQuery query = exe.createBuilder(QueryType.UPDATE)
														.set(properties.toArray(new Property[0]))
														.from(tableName)
														.where(primaryKeysInWhereExpression()).build();
		int isUpdate = exe.executeUpdate(query);
		return isUpdate == 1;
	}
	protected ExpressionInterpreter primaryKeysInWhereExpression() {
		//return new Expression(getPrimaryProperty(null), Operator.EQUAL);
		List<Property> keys = getAllPrimaryProperty(null);
		ExpressionInterpreter and = null;
		ExpressionInterpreter lhr = null;
		for (Property prop : keys) {
			if(lhr == null) {
				lhr = new Expression(prop, Operator.EQUAL);
				and = lhr;
			}else {
				ExpressionInterpreter rhr = new Expression(prop, Operator.EQUAL);
				and = new AndExpression(lhr, rhr);
				lhr = and;
			}
		}
		return and;
	}
	@Override
	public Boolean insert(QueryExecutor exe, String... keys) throws SQLException, Exception {
		List<Property> properties = new ArrayList<>();
		if(keys.length > 0) {
			for (String key : keys) {
				String skey = key.trim();
				Property prop = getProperty(skey, exe, false);
				if (prop == null) {continue;}
				properties.add(prop);
			}
		}else {
			properties = getProperties(exe, false);
		}
		SQLInsertQuery query = exe.createBuilder(QueryType.INSERT)
															.into(Entity.tableName(getClass()))
															.values(properties.toArray(new Property[0])).build();
		
		int insert = exe.executeInsert(isAutoIncrement(), query);
		if(isAutoIncrement()) {
			//update primary key to insert
			updateAutoID(insert);
		}
		return insert >= 1; //0=failed to insert, 1=successful to insert, >1=the auto incremented id which means inserted.
	}
	private void updateAutoID(int insert) throws NoSuchFieldException, IllegalAccessException {
		PrimaryKey pmKey = getPrimaryKey();
		if(pmKey != null && pmKey.name().trim().isEmpty() == false) {
			try {
				Field primaryField = getClass().getDeclaredField(pmKey.name().trim());
				primaryField.setAccessible(true);
				primaryField.set(this, insert);
				primaryField.setAccessible(false);
			} catch (SecurityException | IllegalArgumentException e) {
				e.printStackTrace();
			} 
		}
	}
	@Override
	public Boolean delete(QueryExecutor exe) throws SQLException, Exception {
		//Expression exp = new Expression(getPrimaryProperty(exe), Operator.EQUAL);
		ExpressionInterpreter exp = primaryKeysInWhereExpression();
		SQLDeleteQuery query = exe.createBuilder(QueryType.DELETE)
														.rowsFrom(Entity.tableName(getClass()))
														.where(exp).build();
		int deletedId = exe.executeDelete(query);
		return deletedId == 1;
	}
	///////////////////////////////////////Class API///////////////////////////////////////
	protected static <T extends Entity> boolean shouldAcceptAllProperty(Class<T> type) {
		if(type.isAnnotationPresent(TableName.class) == false) {
			return true;
		}
		TableName tableName = (TableName) type.getAnnotation(TableName.class);
		return tableName.acceptAll();
	}
	protected static <T extends Entity> Map<String, String> mapColumnsToProperties(Class<T> type) {
		boolean acceptAll = Entity.shouldAcceptAllProperty(type);
		if (acceptAll) {return null;}
		
		Map<String, String> result = new HashMap<>();
		for (Field field : type.getDeclaredFields()) {
			if(acceptAll == false
					&& field.isAnnotationPresent(Column.class) == false
					&& field.isAnnotationPresent(PrimaryKey.class) == false) {
				continue;
			}
			if (field.isAnnotationPresent(Column.class)){
				Column column = field.getAnnotation(Column.class);
				String columnName = (column.name().trim().isEmpty() == false) ? column.name().trim() : field.getName();
				field.setAccessible(true);
				result.put(columnName, field.getName());
				field.setAccessible(false);
			}else if(field.isAnnotationPresent(PrimaryKey.class)){
				PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
				String columnName = (primaryKey.name().trim().isEmpty() == false) ? primaryKey.name().trim() : field.getName();
				field.setAccessible(true);
				result.put(columnName, field.getName());
				field.setAccessible(false);
			}
		}
		return result;
	}
	protected static <T extends Entity> String tableName(Class<T> type) {
		if(type.isAnnotationPresent(TableName.class) == false) {
			return type.getSimpleName();
		}
		TableName tableName = (TableName) type.getAnnotation(TableName.class);
		String name = (tableName.value().trim().length() == 0) ? type.getSimpleName() : tableName.value().trim();
		return name;
	}
	public static <T extends Entity> List<T> read(Class<T>  type, QueryExecutor exe, Property...match) throws SQLException, Exception{
		ExpressionInterpreter and = null;
		ExpressionInterpreter lhr = null;
		for (int i = 0; i < match.length; i++) {
			if(lhr == null) {
				lhr = new Expression(match[i], Operator.EQUAL);
				and = lhr;
			}else {
				ExpressionInterpreter rhr = new Expression(match[i], Operator.EQUAL);
				and = new AndExpression(lhr, rhr);
				lhr = and;
			}
		}
		return T.read(type, exe, and);
	}
	public static <T extends Entity> List<T> read(Class<T>  type, QueryExecutor exe, ExpressionInterpreter expression) throws SQLException, Exception{
		String name = Entity.tableName(type);
		SQLSelectQuery query = null;
		if(expression != null) {
			query = exe.createBuilder(QueryType.SELECT)
					.columns()
					.from(name)
					.where(expression).build();
		}else {
			query = exe.createBuilder(QueryType.SELECT)
					.columns()
					.from(name).build();
		}
		//ResultSet set = exe.executeSelect(query);
		//Table table = exe.collection(set);
		//return table.inflate(type, Entity.mapColumnsToProperties(type));
		return exe.executeSelect(query, type, Entity.mapColumnsToProperties(type));
	}
}
