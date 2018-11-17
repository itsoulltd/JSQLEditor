package com.it.soul.lab.sql;

import com.it.soul.lab.sql.query.models.DataType;
import com.it.soul.lab.sql.query.models.Table;

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

        String trimedType = type.trim().toUpperCase();

        if(trimedType.equals("CHAR")
                || trimedType.equals("VARCHAR")
                || trimedType.equals("LONGVARCHAR")){

            return DataType.STRING;

        }
        else if(trimedType.equals("INTEGER")
                || trimedType.equals("BIGINT")
                || trimedType.equals("SMALLINT")){
            return DataType.INT;

        }
        else if(trimedType.equals("DATE")
                || trimedType.equals("DATETIME")){
            return DataType.SQLDATE;

        }else if(trimedType.equals("TIME")
                || trimedType.equals("TIMESTAMP")){
            return DataType.SQLTIMESTAMP;

        }else if(trimedType.equals("FLOAT")){
            return DataType.FLOAT;
        }
        else if(trimedType.equals("DOUBLE")){
            return DataType.DOUBLE;
        }
        else if(trimedType.equals("BIT")
                || trimedType.equals("TINYINT")){
            return DataType.BOOL;
        }
        else if(trimedType.equals("BINARY") || trimedType.equals("VARBINARY") || trimedType.equals("LONGVARBINARY")){
            return DataType.BYTEARRAY;
        }
        else{
            return DataType.OBJECT;
        }

    }
}
