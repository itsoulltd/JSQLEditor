package com.it.soul.lab.sql;

import com.it.soul.lab.sql.entity.Entity;
import com.it.soul.lab.sql.query.models.DataType;
import com.it.soul.lab.sql.query.models.Table;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractExecutor {

    public String toString(Object o){
        StringBuffer buffer = new StringBuffer();
        if(o instanceof List){
            List<?> ox = (List<?>)o;
            for(Object x : ox){
                buffer.append(x.toString() + ";");
                buffer.append('\n');
            }

        }else if(o instanceof Map){
            Map<?,?> ox = (Map<?,?>)o;
            buffer.append(ox.toString());
        }else if(o instanceof Set){
            Set<?> ox = (Set<?>)o;
            buffer.append(ox.toString());
        }else if(o instanceof Table) {
            return toString(((Table)o).getRows());
        }
        return buffer.toString();
    }

    protected DataType convertDataType(String type){
        String trimmedType = type.trim().toUpperCase();
        if(trimmedType.equals("CHAR")
                || trimmedType.equals("VARCHAR")
                || trimmedType.equals("LONGVARCHAR")){
            return DataType.STRING;
        }else if(trimmedType.equals("INTEGER")
                || trimmedType.equals("SMALLINT")){
            return DataType.INT;
        }else if(trimmedType.equals("BIGINT")) {
            return DataType.LONG;
        }else if(trimmedType.equals("DECIMAL")
                || trimmedType.equals("NUMERIC")) {
            return DataType.BIG_DECIMAL;
        }else if(trimmedType.equals("DATE")
                || trimmedType.equals("DATETIME")){
            return DataType.SQLDATE;
        }else if(trimmedType.equals("TIME")
                || trimmedType.equals("TIMESTAMP")){
            return DataType.SQLTIMESTAMP;
        }else if(trimmedType.equals("FLOAT")){
            return DataType.FLOAT;
        }else if(trimmedType.equals("DOUBLE")){
            return DataType.DOUBLE;
        }else if(trimmedType.equals("BIT")
                || trimmedType.equals("TINYINT")){
            return DataType.BOOL;
        }else if(trimmedType.equals("BINARY")
                || trimmedType.equals("VARBINARY")
                || trimmedType.equals("LONGVARBINARY")){
            return DataType.BYTEARRAY;
        }else{
            return DataType.OBJECT;
        }
    }

    protected  <T extends Entity> String getTableName(Class<T> tableType) {
        return Entity.tableName(tableType);
    }

    protected String getDataType(Field field){
        String name = field.getType().getName();
        String compName = getCompatibleDataType(name, field);
        return compName != null ? compName : name.toLowerCase();
    }

    protected String getCompatibleDataType(String name, Field field) {
        name = name.toLowerCase();
        //
        if (field == null || (field.getGenericType() instanceof ParameterizedType) == false){
            //System.out.println("Type: " + field.getType());
            return getSimpleDataType(name);
        }
        //
        ParameterizedType pType = (ParameterizedType)field.getGenericType();;
        //We only care about simple types not user defined types.
        Type[] subscripts = pType.getActualTypeArguments();
        if (name.contains("list")) {
            //return "list<text>";
            Type sub = subscripts.length > 0 ? subscripts[0] : null;
            if (sub != null){
                return "list<" + getSimpleDataType(sub.getTypeName()) + ">";
            }
        }
        else if (name.contains("map")) {
            //return "map<text,text>";
            StringBuffer buffer = new StringBuffer("map<");
            for (Type sub : subscripts) {
                buffer.append(getSimpleDataType(sub.getTypeName()) + ",");
            }
            buffer.replace(buffer.length()-1,buffer.length(),">");
            return buffer.toString();
        }
        else if (name.contains("set")) {
            //return "set<text>";
            Type sub = subscripts.length > 0 ? subscripts[0] : null;
            if (sub != null){
                return "set<" + getSimpleDataType(sub.getTypeName()) + ">";
            }
        }
        //
        return getSimpleDataType(name);
    }

    protected String getSimpleDataType(String name) {
        name = name.toLowerCase();
        if (name.contains("string") || name.contains("text")) return "text";
        if (name.contains("integer")) return "int";
        if (name.contains("int")) return "int";
        if (name.contains("long")) return "bigint";
        if (name.contains("boolean")) return "boolean";
        if (name.contains("double")) return "double";
        if (name.contains("float")) return "float";
        if (name.contains("blob")) return "blob";
        if (name.contains("uuid")) return "uuid";
        if (name.contains("date") || name.contains("time") || name.contains("timestamp")) return "timestamp";
        return null;
    }

}
