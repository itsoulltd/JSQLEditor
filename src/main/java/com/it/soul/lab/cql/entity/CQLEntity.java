package com.it.soul.lab.cql.entity;

import com.it.soul.lab.cql.query.CQLInsertQuery;
import com.it.soul.lab.sql.QueryExecutor;
import com.it.soul.lab.sql.entity.Column;
import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.entity.PrimaryKey;
import com.it.soul.lab.sql.query.QueryType;
import com.it.soul.lab.sql.query.SQLSelectQuery;
import com.it.soul.lab.sql.query.models.*;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

public abstract class CQLEntity extends Entity {

    @Override
    protected boolean hasColumnAnnotationPresent(Field field) {
        return super.hasColumnAnnotationPresent(field) || field.isAnnotationPresent(ClusteringKey.class);
    }

    @Override
    protected Property getProperty(String fieldName, QueryExecutor exe, boolean skipPrimary){
        Property prop = super.getProperty(fieldName, exe, skipPrimary);
        try {
            Field field = getClass().getDeclaredField(fieldName);
            if (field.isAnnotationPresent(ClusteringKey.class)){
                if (skipPrimary) return null;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return prop;
    }

    @Override
    protected List<Field> getPrimaryFields() {
        List<Field> fields = super.getPrimaryFields();
        Field[] allAgain = this.getClass().getDeclaredFields();
        for (Field fl : allAgain) {
            if (fl.isAnnotationPresent(ClusteringKey.class)){
                fields.add(fl);
            }
        }
        return fields;
    }

    @Override
    protected String getPropertyKey(Field field) {
        //Also consider @ClusteringKey:name() is also present.
        if(field.isAnnotationPresent(ClusteringKey.class)) {
            ClusteringKey column = field.getAnnotation(ClusteringKey.class);
            String clName = column.name().trim();
            return (!clName.isEmpty()) ? clName : field.getName();
        }else {
            return super.getPropertyKey(field);
        }
    }

    protected long getTTLValue(){
        if (getClass().isAnnotationPresent(EnableTimeToLive.class)) {
            return getClass().getAnnotation(EnableTimeToLive.class).value();
        }
        return 0;
    }

    @Override
    public Boolean insert(QueryExecutor exe, String... keys) throws SQLException {
        //
        List<Property> properties = getPropertiesFromKeys(exe, keys, false);
        CQLInsertQuery query = exe.createQueryBuilder(QueryType.INSERT)
                .into(Entity.tableName(getClass()))
                .values(properties.toArray(new Property[0])).build();

        long ttl = getTTLValue();
        if (ttl > 0L){
            query.usingTTL(ttl);
        }

        int result = exe.executeInsert(false, query);
        return result == 1;
    }

    public static <T extends Entity> Map<String, String> mapColumnsToProperties(Class<T> type) {
        Map<String, String> results = new HashMap<>();
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)){
                Column column = field.getAnnotation(Column.class);
                String columnName = (column.name().trim().isEmpty() == false) ? column.name().trim() : field.getName();
                results.put(columnName, field.getName());
            }else if(field.isAnnotationPresent(PrimaryKey.class)){
                PrimaryKey primaryKey = field.getAnnotation(PrimaryKey.class);
                String columnName = (primaryKey.name().trim().isEmpty() == false) ? primaryKey.name().trim() : field.getName();
                results.put(columnName, field.getName());
            }else if (field.isAnnotationPresent(ClusteringKey.class)){
                ClusteringKey column = field.getAnnotation(ClusteringKey.class);
                String columnName = (column.name().trim().isEmpty() == false) ? column.name().trim() : field.getName();
                results.put(columnName, field.getName());
            }else{
                results.put(field.getName(), field.getName());
            }
        }
        return results;
    }

    public static <T extends Entity> List<T> read(Class<T>  type, QueryExecutor exe, Property...match) throws SQLException, Exception{
        //We will add our cassandra specific search key.
        List<Property> properties = validateProperties(type, exe, Arrays.asList(match));
        ExpressionInterpreter expression = getExpressionInterpreter(properties);
        //
        String name = Entity.tableName(type);
        SQLSelectQuery query = getSqlSelectQuery(exe, expression, name);
        return exe.executeSelect(query, type, CQLEntity.mapColumnsToProperties(type));
    }

    public static <T extends Entity> List<T> read(Class<T>  type, QueryExecutor exe, ExpressionInterpreter expression) throws SQLException, Exception{
        //We will add our cassandra specific search key.
        expression = validateExpressions(type, exe, expression);

        String name = Entity.tableName(type);
        SQLSelectQuery query = getSqlSelectQuery(exe, expression, name);
        return exe.executeSelect(query, type, CQLEntity.mapColumnsToProperties(type));
    }

    private static SQLSelectQuery getSqlSelectQuery(QueryExecutor exe, ExpressionInterpreter expression, String name) {
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
        return query;
    }

    protected static <T extends Entity> ExpressionInterpreter validateExpressions(Class<T> type, QueryExecutor exe, ExpressionInterpreter expression) {
        Expression[] exps = expression.resolveExpressions();
        List<Property> props = new ArrayList<>();
        for (Expression exp : exps) {
            props.add(exp.getValueProperty());
        }
        List<Property> properties = validateProperties(type, exe, props);
        ExpressionInterpreter interpreter = getExpressionInterpreter(properties);
        return interpreter;
    }

    protected static ExpressionInterpreter getExpressionInterpreter(List<Property> match) {
        ExpressionInterpreter and = null;
        ExpressionInterpreter lhr = null;
        for (int i = 0; i < match.size(); i++) {
            if(lhr == null) {
                lhr = new Expression(match.get(i), Operator.EQUAL);
                and = lhr;
            }else {
                ExpressionInterpreter rhr = new Expression(match.get(i), Operator.EQUAL);
                and = new AndExpression(lhr, rhr);
                lhr = and;
            }
        }
        return and;
    }

    protected static <T extends Entity> List<Property> validateProperties(Class<T> type, QueryExecutor exe, List<Property> match) {
        List<Property> results = new ArrayList<>();
        try {
            //Not efficient way of sorting:
            Entity instance = type.newInstance();
            if (instance instanceof CQLEntity){
                List<Property> primaryProps = ((CQLEntity)instance).getPrimaryProperties(exe);
                int index = 0;
                while (index < primaryProps.size() && index < match.size()){
                    Property pProperty = primaryProps.get(index);
                    Property mProperty = match.get(index);
                    if (mProperty.getKey().equalsIgnoreCase(pProperty.getKey())){
                        results.add(mProperty);
                    }
                    index++;
                }
            }
            instance = null;
        }catch (Exception e){
            e.printStackTrace();
        }
        return results;
    }

}
