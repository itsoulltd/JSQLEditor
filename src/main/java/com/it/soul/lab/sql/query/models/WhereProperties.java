package com.it.soul.lab.sql.query.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhereProperties extends Row {

    @Override
    public Row add(Property prop){
        if (prop == null) {return this;}
        getProperties().add(prop);
        return this;
    }
    public String[] getKeys(){
        //Before Java 8
        //This does a shallow copy
        int paddingCount = 1;
        List<String> result = new ArrayList<String>();
        String lastKey = null;
        for (Property x : this.getProperties()) {
            String newKey = new String(x.getKey());
            if (lastKey != null && lastKey.equalsIgnoreCase(newKey)){
                newKey = newKey + "_" + paddingCount++;
            }
            result.add(newKey);
            lastKey = newKey;
        }
        return result.toArray(new String[]{});
    }
    public Map<String, Property> keyValueMap(){
        //This does a shallow copy
        int paddingCount = 1;
        Map<String, Property> result = new HashMap<String, Property>();
        String lastKey = null;
        for (Property parameter : this.getProperties()) {
            String newKey = new String(parameter.getKey());
            if (lastKey != null && lastKey.equalsIgnoreCase(newKey)){
                newKey = newKey + "_" + paddingCount++;
            }
            result.put(newKey, new Property(parameter));
            lastKey = newKey;
        }
        return result;
    }
}
