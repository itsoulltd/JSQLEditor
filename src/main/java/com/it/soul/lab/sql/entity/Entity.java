package com.it.soul.lab.sql.entity;

import com.it.soul.lab.sql.QueryExecutor;
import com.it.soul.lab.sql.query.*;
import com.it.soul.lab.sql.query.models.*;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class Entity implements EntityInterface{
	public Entity() {
		super();
	}

	protected List<Property> getProperties(QueryExecutor exe, boolean skipPrimary) {
		List<Property> result = new ArrayList<>();
		boolean acceptAll = shouldAcceptAllAsProperty();
		Field[] fields = getDeclaredFields(true);
		for (Field field : fields) {
			if(acceptAll == false && hasColumnAnnotationPresent(field) == false) {
				continue;
			}
			if (field.isAnnotationPresent(Ignore.class))
				continue;
			Property prop = getProperty(field.getName(), exe, skipPrimary);
			if(prop == null) {continue;}
			result.add(prop);
		}
		return result;
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
			Field field = getDeclaredField(fieldName, true);
			if(field.isAnnotationPresent(PrimaryKey.class)) {
				if (skipPrimary) {return null;}
				if ((field.getAnnotation(PrimaryKey.class)).auto()) {return null;}
			}
			field.setAccessible(true);
			Object value = getFieldValue(field, exe);
			//Here, we are skipping null value too.
			//if (value == null) return null;
			String actualKey = getPropertyKey(field);
			result = new Property(actualKey, value);
			field.setAccessible(false);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public final Field getDeclaredField(String fieldName, boolean inherit) throws NoSuchFieldException{
        //Search for the field, until get found any of the super class.
        //But not infinite round: MAX=8
        int maxLoopCount = 8;
        int loopCount = 0;
        Field field = null;
        Class mySClass = getClass();
        do{
            try{
                field = mySClass.getDeclaredField(fieldName);
            }catch (NoSuchFieldException | SecurityException e) {
                if (inherit == false) throw e;
                else mySClass = mySClass.getSuperclass();
            }
            if (loopCount++ > maxLoopCount) throw new NoSuchFieldException( fieldName + " does't exist!");
        }while(field == null);
        //
        return field;
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

	private DataType getFieldDataType(Field field) {
		if (field.isAnnotationPresent(Column.class)){
			Column annotation = field.getAnnotation(Column.class);
			DataType type = annotation.type();
			return type;
		}else{
			Class type = field.getType();
			if (Integer.class.isAssignableFrom(type)){
				return DataType.INT;
			}else if (Boolean.class.isAssignableFrom(type)){
				return DataType.BOOL;
			}else if (Float.class.isAssignableFrom(type)){
				return DataType.FLOAT;
			}else if (Double.class.isAssignableFrom(type)){
				return DataType.DOUBLE;
			}else if (String.class.isAssignableFrom(type)){
				return DataType.STRING;
			}else if (java.util.Date.class.isAssignableFrom(type) ||
					java.sql.Date.class.isAssignableFrom(type)){
				return DataType.SQLDATE;
			}else if (Time.class.isAssignableFrom(type) ||
					Timestamp.class.isAssignableFrom(type)){
				return DataType.SQLTIMESTAMP;
			}else if (Long.class.isAssignableFrom(type)){
				return DataType.LONG;
			}else if (Blob.class.isAssignableFrom(type)){
				return DataType.BLOB;
			}else{
				return DataType.OBJECT;
			}
		}
	}

    protected boolean hasColumnAnnotationPresent(Field field) {
        boolean isAnnotated = field.isAnnotationPresent(Column.class)
                || field.isAnnotationPresent(PrimaryKey.class)
				|| field.isAnnotationPresent(javax.persistence.Column.class);
        return isAnnotated;
    }

    public final Field[] getDeclaredFields(boolean inherit){
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(getClass().getDeclaredFields()));
        if (inherit){
            //Inherit properties from one immediate parent which is not Entity.class.
            /*if (!getClass().getSuperclass().getSimpleName().equalsIgnoreCase(Entity.class.getSimpleName())){
                fields.addAll(Arrays.asList(getClass().getSuperclass().getDeclaredFields()));
            }*/
            Class mySuperClass = getClass().getSuperclass();
            while(!mySuperClass.getSimpleName().equalsIgnoreCase(Entity.class.getSimpleName())){
                fields.addAll(Arrays.asList(mySuperClass.getDeclaredFields()));
                mySuperClass = mySuperClass.getSuperclass();
            }
        }
        return fields.toArray(new Field[0]);
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
		}else if(field.isAnnotationPresent(javax.persistence.Column.class)) {
			javax.persistence.Column column = field.getAnnotation(javax.persistence.Column.class);
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
            _isAutoIncremented = false;
			PrimaryKey primAnno = getPrimaryKey();
			if(primAnno != null) {
				_isAutoIncremented = primAnno.auto();
			}
			Field genField = getGeneratedValueField();
			if (!_isAutoIncremented && genField != null){
                _isAutoIncremented = true;
            }
		}
		return _isAutoIncremented;
	}

	private Field getGeneratedValueField(){
	    Field[] fields = getDeclaredFields(true);
	    Field fl = null;
        for (Field field: fields) {
            if (field.isAnnotationPresent(GeneratedValue.class)){
                fl = field;
                break;
            }
        }
        return fl;
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

    protected List<Field> getPrimaryFields() {
        List<Field> keys = new ArrayList<>();
        Field[] fields = getDeclaredFields(true);
        for (Field field : fields) {
            if(field.isAnnotationPresent(PrimaryKey.class)) {
                keys.add(field);
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

    private Property getPrimaryProperty(QueryExecutor exe) {
	    List<Property> all = getPrimaryProperties(exe);
        Property result = (all.size() > 0) ? all.get(0 ) : null;
        return result;
    }

    public Map<String, Object> marshallingToMap(boolean inherit) {
        Map<String, Object> result = new HashMap<>();
        for (Field field : getDeclaredFields(inherit)) {
			if (field.isAnnotationPresent(Ignore.class))
				continue;
			try {
				field.setAccessible(true);
				//Notice:We are interested into reading just the filed name:value into a map.
				try {
					Object fieldValue = field.get(this);
					if (fieldValue != null && EntityInterface.class.isAssignableFrom(fieldValue.getClass())){
						EntityInterface enIf = (EntityInterface) fieldValue;
						result.put(field.getName(), enIf.marshallingToMap(inherit));
					}else {
						result.put(field.getName(), fieldValue);
					}
				} catch (IllegalAccessException | IllegalArgumentException e) {}
				field.setAccessible(false);
			} catch (SecurityException e) {}
		}
        return result;
    }

	public void unmarshallingFromMap(Map<String, Object> data, boolean inherit){
		if (data != null) {
			Field[] fields = getDeclaredFields(inherit);
			for (Field field : fields) {
                if (field.isAnnotationPresent(Ignore.class))
                    continue;
				try {
					field.setAccessible(true);
					Object entry = data.get(field.getName());
					if(entry != null) {
						try {
							if (EntityInterface.class.isAssignableFrom(field.getType())){
								//Now we can say this might-be a marshaled object that confirm to EntityInterface,
								EntityInterface enIf = (EntityInterface) field.getType().newInstance();
								if(entry instanceof Map)
									enIf.unmarshallingFromMap((Map<String, Object>) entry, true);
								field.set(this, enIf);
							}else{
								field.set(this, entry);
							}
						} catch (Exception e) {}
					}
					field.setAccessible(false);
				} catch (SecurityException e) {}
			}
		}
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
	public Boolean update(QueryExecutor exe, String...keys) throws SQLException {
		List<Property> properties = getPropertiesFromKeys(exe, keys, true);
		String tableName = Entity.tableName(getClass());
		SQLUpdateQuery query = exe.createQueryBuilder(QueryType.UPDATE)
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
	public Boolean insert(QueryExecutor exe, String... keys) throws SQLException {
		List<Property> properties = getPropertiesFromKeys(exe, keys, isAutoIncrement());
		SQLInsertQuery query = exe.createQueryBuilder(QueryType.INSERT)
															.into(Entity.tableName(getClass()))
															.values(properties.toArray(new Property[0])).build();
		
		int result = exe.executeInsert(isAutoIncrement(), query);
		if( result > 1 || isAutoIncrement()) {
			//update primary key to insert
			try {
				updateAutoID(result);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return result >= 1; //0=failed to insert, 1=successful to insert, >1=the auto incremented id which means inserted.
	}

	protected List<Property> getPropertiesFromKeys(QueryExecutor exe, String[] keys, boolean skipPrimary) {
		List<Property> properties = new ArrayList<>();
		if (keys.length > 0) {
			for (String key : keys) {
				String skey = key.trim();
				Property prop = getProperty(skey, exe, skipPrimary);
				if (prop == null) {
					continue;
				}
				properties.add(prop);
			}
		} else {
			properties = getProperties(exe, skipPrimary);
		}
		return properties;
	}

	private void updateAutoID(int insert) throws IllegalAccessException {
		List<Field> primaryFields = getPrimaryFields();
		if (primaryFields.isEmpty()) return;

		try {
			//Update any primary field that has auto = yes
			for (Field primaryField : primaryFields) {
				if (primaryField.isAnnotationPresent(PrimaryKey.class) == false) continue;
				if (primaryField.getAnnotation(PrimaryKey.class).auto() == false) continue;
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
	public Boolean delete(QueryExecutor exe) throws SQLException {
		//Expression exp = new Expression(getPrimaryProperty(exe), Operator.EQUAL);
		ExpressionInterpreter exp = primaryKeysInWhereExpression(exe);
		SQLDeleteQuery query = exe.createQueryBuilder(QueryType.DELETE)
														.rowsFrom(Entity.tableName(getClass()))
														.where(exp).build();
		int deletedId = exe.executeDelete(query);
		return deletedId == 1;
	}

    @Override
    public String tableName() {
        return Entity.tableName(this.getClass());
    }

    ///////////////////////////////////////Class API///////////////////////////////////////
	protected static <T extends Entity> boolean shouldAcceptAllAsProperty(Class<T> type) {
		if(type.isAnnotationPresent(TableName.class)) {
            TableName tableName = type.getAnnotation(TableName.class);
            return tableName.acceptAll();
        }
        if (type.isAnnotationPresent(javax.persistence.Entity.class)){
		    return true;
        }
        return false;
	}

    protected final static <T extends Entity> Field[] getDeclaredFields(Class<T> type, boolean inherit){
        Field[] fields = new Field[0];
        try {
            T newInstance = type.newInstance();
            fields = newInstance.getDeclaredFields(inherit);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return fields;
    }

	public static <T extends Entity> Map<String, String> mapColumnsToProperties(Class<T> type) {

		boolean acceptAll = Entity.shouldAcceptAllAsProperty(type);
		if (acceptAll) {return null;}
		
		Map<String, String> result = new HashMap<>();
		for (Field field : Entity.getDeclaredFields(type, true)) {
			if(acceptAll == false
					&& field.isAnnotationPresent(Column.class) == false
					&& field.isAnnotationPresent(javax.persistence.Column.class) == false
					&& field.isAnnotationPresent(PrimaryKey.class) == false) {
				continue;
			}
            if (field.isAnnotationPresent(Ignore.class))
                continue;
			if (field.isAnnotationPresent(Column.class)){
				Column column = field.getAnnotation(Column.class);
				String columnName = (column.name().trim().isEmpty() == false) ? column.name().trim() : field.getName();
				result.put(columnName, field.getName());
			}else if(field.isAnnotationPresent(PrimaryKey.class)){
				PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
				String columnName = (primaryKey.name().trim().isEmpty() == false) ? primaryKey.name().trim() : field.getName();
				result.put(columnName, field.getName());
			}else if (field.isAnnotationPresent(javax.persistence.Column.class)){
                javax.persistence.Column column = field.getAnnotation(javax.persistence.Column.class);
                String columnName = (column.name().trim().isEmpty() == false) ? column.name().trim() : field.getName();
                result.put(columnName, field.getName());
            }
		}
		return result;
	}

	public static <T extends Entity> String tableName(Class<T> type) {
		if (type.isAnnotationPresent(TableName.class)){
			TableName tableName = type.getAnnotation(TableName.class);
			String name = (tableName.value().trim().isEmpty()) ? type.getSimpleName() : tableName.value().trim();
			return name;
		}else if (type.isAnnotationPresent(Table.class)){
			Table tableName = type.getAnnotation(Table.class);
			String name = (tableName.name().trim().isEmpty()) ? type.getSimpleName() : tableName.name().trim();
			return name;
		}else {
			return type.getSimpleName();
		}
	}

	public static <T extends Entity> boolean isAutoID(Class<T> type) {
		boolean result = false;
		for (Field field : type.getDeclaredFields()) {
			if (field.isAnnotationPresent(PrimaryKey.class)){
				PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
				result = primaryKey.auto();
				break;
			}else if (field.isAnnotationPresent(GeneratedValue.class)){
				GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
				result = (generatedValue.strategy() == GenerationType.AUTO
							|| generatedValue.strategy() == GenerationType.IDENTITY);
				break;
			}
		}
		return result;
	}

	public static <T extends Entity> List<T> read(Class<T>  type
			, QueryExecutor exe
			, Property...match)
			throws Exception{
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

	public static <T extends Entity> List<T> read(Class<T>  type
			, QueryExecutor exe
			, ExpressionInterpreter expression)
			throws Exception{
		String name = Entity.tableName(type);
		SQLSelectQuery query = null;
		if(expression != null) {
			query = exe.createQueryBuilder(QueryType.SELECT)
					.columns()
					.from(name)
					.where(expression).build();
		}else {
			query = exe.createQueryBuilder(QueryType.SELECT)
					.columns()
					.from(name).build();
		}
		return exe.executeSelect(query, type, Entity.mapColumnsToProperties(type));
	}

	public static <T extends Entity> void read(Class<T> aClass
			, QueryExecutor executor
			, int pageSize
			, ExpressionInterpreter expression
			, Consumer<List<T>> consumer){
		//
		read(aClass, executor, pageSize, -1, expression, consumer);
	}

	public static <T extends Entity> void read(Class<T> aClass
			, QueryExecutor executor
			, int pageSize
			, int rowCount
			, ExpressionInterpreter expression
			, Consumer<List<T>> consumer){
		//
		read(aClass, executor, 0, pageSize, rowCount, expression, consumer);
	}

	public static <T extends Entity> void read(Class<T> aClass
			, QueryExecutor executor
			, int offset
			, int pageSize
			, int rowCount
			, ExpressionInterpreter expression
			, Consumer<List<T>> consumer){
		//
		if (consumer == null) return;
		try {
			List<SQLSelectQuery> queries = createSelectQueries(aClass, executor, offset, pageSize, rowCount, expression);
			for (SQLSelectQuery query : queries) {
				try {
					List<T> items = executor.executeSelect(query, aClass, Entity.mapColumnsToProperties(aClass));
					consumer.accept(items);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static List<SQLSelectQuery> createSelectQueries(Class<? extends Entity> aClass
			, QueryExecutor executor
			, int offset
			, int pageSize
			, int rowCount
			, ExpressionInterpreter expression)
			throws SQLException {
		//Creating Paged SelectQueries
		List<SQLSelectQuery> queries = new ArrayList<>();
		SQLScalarQuery countQuery = executor.createQueryBuilder(QueryType.COUNT)
				.columns()
				.on(Entity.tableName(aClass))
				.build();

		int maxCount = executor.getScalarValue(countQuery);
		rowCount = (rowCount > 0 && rowCount < maxCount) ? rowCount : maxCount;
		//int loopCount = (rowCount % 2 == 0) ? (rowCount / pageSize) : ((rowCount / pageSize) + 1);
		int loopCount = Double.valueOf(Math.ceil((rowCount * 1.0d / pageSize))).intValue();
		int index = 0;
		while (index < loopCount){
			SQLSelectQuery query;
			if (expression != null){
				query = executor.createQueryBuilder(QueryType.SELECT)
						.columns()
						.from(Entity.tableName(aClass))
						.where(expression)
						.addLimit(pageSize, offset).build();
			}else {
				query = executor.createQueryBuilder(QueryType.SELECT)
						.columns()
						.from(Entity.tableName(aClass))
						.addLimit(pageSize, offset).build();
			}
			//Add Query:
			if(query != null)
				queries.add(query);
			//
			offset += pageSize;
			index++;
		}
		return queries;
	}

	public static <T extends Entity> void read(Class<T> aClass
			, QueryExecutor executor
			, int pageSize
			, int rowCount
			, Property pagingKey
			, Operator sortOrder
			, WherePredicate predicate
			, Consumer<List<T>> consumer) {
		//
		if (consumer == null) throw new RuntimeException("Consumer is empty!");
		if (predicate == null) throw new RuntimeException("expression is empty!");
		if (pagingKey == null) throw new RuntimeException("pagingKey is empty!");
		try {
			int fetchCount = 0;
			ExpressionInterpreter expression;
			Property nextKey = pagingKey;
			do {
				//Now insert the pagingKey into expression, So that we can read next batch:
				expression = predicate.apply(nextKey);
				//Fetch from Cassandra:
				SQLSelectQuery query = executor.createQueryBuilder(QueryType.SELECT)
						.columns()
						.from(Entity.tableName(aClass))
						.where(expression)
						.orderBy(sortOrder, pagingKey.getKey())
						.addLimit(pageSize, 0)
						.build();
				List<T> items = executor.executeSelect(query
						, aClass
						, Entity.mapColumnsToProperties(aClass));
				consumer.accept(items);
				//If Items are empty that means end of Fetch:
				if (items.isEmpty()) {
					break;
				}
				//Otherwise prepare next query with new next batch using pagingKey:
				T lastItem = items.get(items.size() - 1);
				Property possibleNextKey = lastItem.getRow().keyValueMap().get(pagingKey.getKey());
				//If both values are equal then end of Fetch:
				if (Objects.equals(nextKey.getValue(), possibleNextKey.getValue())){
					break;
				}
				nextKey = possibleNextKey;
				fetchCount += pageSize;
			} while (fetchCount < rowCount);
		} catch (SQLException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
			consumer.accept(Collections.emptyList());
		}
	}

	public static Row getRowDefinition(Class<? extends Entity> aClass, String...skipColumns) {
		Row row = new Row();
		List<String> skipList = Arrays.asList(skipColumns);
		Map<String, String> columnNameMap = mapColumnsToProperties(aClass);
		if (columnNameMap != null && !columnNameMap.isEmpty()){
			//Map<columnName, fieldName> : mapColumnsToProperties(...)
			for (Map.Entry<String, String> entry : columnNameMap.entrySet()) {
				String columnName = entry.getKey();
				if (!skipList.contains(columnName))
					row.add(columnName);
			}
		}else{
			for (Field field : Entity.getDeclaredFields(aClass, true)) {
				if (field.isAnnotationPresent(Ignore.class))
					continue;
				if (!skipList.contains(field.getName()))
					row.add(field.getName());
			}
		}
		return row;
	}

	public Row getRow(String...skipColumns) {
		Row row = new Row();
		Class<? extends Entity> aClass = this.getClass();
		List<String> skipList = Arrays.asList(skipColumns);
		Map<String, Object> data = marshallingToMap(true);
		//
		Map<String, String> columnNameMap = mapColumnsToProperties(aClass);
		if (columnNameMap != null && !columnNameMap.isEmpty()){
			//Map<columnName, fieldName> : mapColumnsToProperties(...)
			for (Map.Entry<String, String> entry : columnNameMap.entrySet()) {
				String columnName = entry.getKey();
				String fieldName = entry.getValue();
				if (!skipList.contains(columnName))
					row.add(columnName, data.get(fieldName));
			}
		}else {
			for (Field field : Entity.getDeclaredFields(aClass, true)) {
				if (field.isAnnotationPresent(Ignore.class))
					continue;
				if (!skipList.contains(field.getName()))
					row.add(field.getName(), data.get(field.getName()));
			}
		}
		return row;
	}

	public static <T extends Entity> Map<String, List<T>> groupBy(String groupByKey, List<T> rows){
		Map<String, List<T>> results = new ConcurrentHashMap<>();
		for (T row : rows) {
			Map<String, Object> map = row.marshallingToMap(true);
			String theKey = map.get(groupByKey).toString(); //key's value going to be the results-key
			List<T> items = results.get(theKey);
			if (items == null) {
				items = new ArrayList<>();
				results.put(theKey, items);
			}
			//
			items.add(row);
		}
		return results;
	}

	public static void insert(Class<? extends Entity> entityType
			, QueryExecutor executor
			, int batchSize, List<Row> batch) throws SQLException {
		if (batch.isEmpty()) return;
		if (batchSize <= 0) batchSize = 10;
		List<Property> cols = batch.get(0).getProperties();
		SQLQuery query = executor.createQueryBuilder(QueryType.INSERT)
				.into(entityType)
				.values(cols.toArray(new Property[0]))
				.build();
		executor.executeInsert(Entity.isAutoID(entityType), batchSize, query, batch);
	}

	public static void update(Class<? extends Entity> entityType
			, QueryExecutor executor
			, ExpressionInterpreter clause, Row row) throws SQLException {
		List<Property> cols = row.getProperties();
		if (cols.isEmpty()) return;
		SQLQuery query = executor.createQueryBuilder(QueryType.UPDATE)
				.set(cols.toArray(new Property[0]))
				.from(entityType)
				.where(clause)
				.build();
		executor.executeUpdate(query);
	}

	public static void delete(Class<? extends Entity> entityType
			, QueryExecutor executor
			, ExpressionInterpreter clause) throws SQLException {
		SQLQuery query = executor.createQueryBuilder(QueryType.DELETE)
				.rowsFrom(entityType)
				.where(clause)
				.build();
		executor.executeDelete(query);
	}

	public static int count(Class<? extends Entity> entityType
			, QueryExecutor executor
			, ExpressionInterpreter clause) throws SQLException {
		SQLQuery query = (clause == null)
				? executor.createQueryBuilder(QueryType.COUNT).columns().from(entityType).build()
				: executor.createQueryBuilder(QueryType.COUNT).columns().from(entityType).where(clause).build();
		return executor.getScalarValue(query);
	}

}
