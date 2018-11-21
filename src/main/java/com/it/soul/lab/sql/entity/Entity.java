package com.it.soul.lab.sql.entity;

import com.it.soul.lab.sql.QueryExecutor;
import com.it.soul.lab.sql.query.SQLDeleteQuery;
import com.it.soul.lab.sql.query.SQLInsertQuery;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLSelectQuery;
import com.it.soul.lab.sql.query.SQLUpdateQuery;
import com.it.soul.lab.sql.query.models.*;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Entity implements EntityInterface{
	public Entity() {
		super();
	}
	protected boolean isFieldAnnotatedWith(Field field) {
		boolean isAnnotated = field.isAnnotationPresent(Column.class)
				|| field.isAnnotationPresent(PrimaryKey.class);
		return isAnnotated;
	}
	protected List<Property> getProperties(QueryExecutor exe, boolean skipPrimary) {
		List<Property> result = new ArrayList<>();
		boolean acceptAll = shouldAcceptAllAsProperty();
		for (Field field : this.getClass().getDeclaredFields()) {
			if(acceptAll == false && isFieldAnnotatedWith(field) == false) {
				continue;
			}
			Property prop = getProperty(field.getName(), exe, skipPrimary);
			if(prop == null) {continue;}
			result.add(prop);
		}
		return result;
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

	/**
	 *
	 * @param fieldName : must have to be the associated field name:
	 * @param exe
	 * @param skipPrimary
	 * @return
	 */
	protected Property getProperty(String fieldName, QueryExecutor exe, boolean skipPrimary) {
		Property result = null;
		try {
			Field field = this.getClass().getDeclaredField(fieldName);
			if(field.isAnnotationPresent(PrimaryKey.class)) {
				if (skipPrimary) {return null;}
				if (((PrimaryKey)field.getAnnotation(PrimaryKey.class)).autoIncrement() == true
						 && skipPrimary != false) {return null;}
			}
			field.setAccessible(true);
			String actualKey = getPropertyKey(field);
			Object value = getFieldValue(field, exe);
			result = new Property(actualKey, value);
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
		//Also consider @PrimaryKey:name() is also present.
		if(field.isAnnotationPresent(Column.class)) {
			Column column = field.getAnnotation(Column.class);
			String clName = column.name().trim();
			return (!clName.isEmpty()) ? clName : field.getName();
		}else if(field.isAnnotationPresent(PrimaryKey.class)) {
			PrimaryKey pm = field.getAnnotation(PrimaryKey.class);
			String pmName = pm.name().trim();
			return (!pmName.isEmpty()) ? pmName : field.getName();
		}else {
			return field.getName();
		}
	}
	protected boolean shouldAcceptAllAsProperty() {
		return Entity.shouldAcceptAllAsProperty(this.getClass());
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
	protected List<Field> getPrimaryFields() {
		List<Field> keys = new ArrayList<>();
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			if(field.isAnnotationPresent(PrimaryKey.class)) {
				keys.add(field);
			}
		}
		return keys;
	}
	private PrimaryKey getPrimaryKey() {
		PrimaryKey key = null;
		List<Field> fields = getPrimaryFields();
		for (Field field : fields) {
			if(field.isAnnotationPresent(PrimaryKey.class)) {
				key = field.getAnnotation(PrimaryKey.class);
				break;
			}
		}
		return key;
	}
	private Property getPrimaryProperty(QueryExecutor exe) {
		Property result = null;
		try {
			List<Field> primaryFields = getPrimaryFields();
			if (primaryFields.isEmpty()) return result;
			//
			String key = primaryFields.get(0).getName();
			result = getProperty(key, exe, false);
		} catch (SecurityException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return result;
	}
	private List<PrimaryKey> getPrimaryKeys() {
		List<PrimaryKey> keys = new ArrayList<>();
		List<Field> fields = getPrimaryFields();
		for (Field field : fields) {
			if(field.isAnnotationPresent(PrimaryKey.class)) {
				keys.add(field.getAnnotation(PrimaryKey.class));
			}
		}
		return keys;
	}
	protected List<Property> getPrimaryProperties(QueryExecutor exe) {
		List<Property> results = new ArrayList<>();
		try {
			List<Field> primaryFields = getPrimaryFields();
			if (primaryFields.isEmpty()) return results;
			//
			for (Field pmKeyField : primaryFields){
				String key = pmKeyField.getName();
				results.add(getProperty(key, exe, false));
			}
		} catch (SecurityException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		return results;
	}

	/**
	 *
	 * @param exe
	 * @param keys
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	@Override
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
														.where(primaryKeysInWhereExpression(exe)).build();
		int isUpdate = exe.executeUpdate(query);
		return isUpdate == 1;
	}
	protected ExpressionInterpreter primaryKeysInWhereExpression(QueryExecutor exe) {
		//return new Expression(getPrimaryProperty(null), Operator.EQUAL);
		List<Property> keys = getPrimaryProperties(exe);
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

	/**
	 *
	 * @param exe
	 * @param keys : must be field names associated with this class.
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
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
		
		int result = exe.executeInsert(isAutoIncrement(), query);
		if( result > 1 || isAutoIncrement()) {
			//update primary key to insert
			updateAutoID(result);
		}
		return result >= 1; //0=failed to insert, 1=successful to insert, >1=the auto incremented id which means inserted.
	}
	private void updateAutoID(int insert) throws NoSuchFieldException, IllegalAccessException {
		List<Field> primaryFields = getPrimaryFields();
		if (primaryFields.isEmpty()) return;

		try {
			//Update any primary field that has autoIncrement = yes
			for (Field primaryField : primaryFields) {
				if (primaryField.isAnnotationPresent(PrimaryKey.class) == false) continue;
				if (primaryField.getAnnotation(PrimaryKey.class).autoIncrement() == false) continue;
				primaryField.setAccessible(true);
				primaryField.set(this, insert);
				primaryField.setAccessible(false);
				break;
			}
		} catch (SecurityException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param exe
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	@Override
	public Boolean delete(QueryExecutor exe) throws SQLException, Exception {
		//Expression exp = new Expression(getPrimaryProperty(exe), Operator.EQUAL);
		ExpressionInterpreter exp = primaryKeysInWhereExpression(exe);
		SQLDeleteQuery query = exe.createBuilder(QueryType.DELETE)
														.rowsFrom(Entity.tableName(getClass()))
														.where(exp).build();
		int deletedId = exe.executeDelete(query);
		return deletedId == 1;
	}
	///////////////////////////////////////Class API///////////////////////////////////////
	protected static <T extends Entity> boolean shouldAcceptAllAsProperty(Class<T> type) {
		if(type.isAnnotationPresent(TableName.class) == false) {
			return true;
		}
		TableName tableName = (TableName) type.getAnnotation(TableName.class);
		return tableName.acceptAll();
	}
	protected static <T extends Entity> Map<String, String> mapColumnsToProperties(Class<T> type) {
		boolean acceptAll = Entity.shouldAcceptAllAsProperty(type);
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
				result.put(columnName, field.getName());
			}else if(field.isAnnotationPresent(PrimaryKey.class)){
				PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
				String columnName = (primaryKey.name().trim().isEmpty() == false) ? primaryKey.name().trim() : field.getName();
				result.put(columnName, field.getName());
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
		return exe.executeSelect(query, type, Entity.mapColumnsToProperties(type));
	}
}
