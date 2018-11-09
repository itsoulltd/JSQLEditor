package com.it.soul.lab.sql.query.models;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.it.soul.lab.sql.query.models.DataType;

public class Row {
	
	public Row() {
		super();
	}
	public List<Property> getProperties() {
		return properties;
	}
	public void setProperties(List<Property> items) {
		if(items == null) {return;}
		this.properties = items;
	}
	private List<Property> properties = new ArrayList<Property>();
	public Row add(Property prop){
		if (prop == null || properties.contains(prop) == true) {return this;}
		properties.add(prop);
		return this;
	}
	public Row add(String name){
		return add(new Property(name));
	}
	public Row add(String name, Object value, DataType type){
		return add(new Property(name, value, type));
	}
	public List<Property> getCloneProperties(){
		//All standard collections have copy constructors.
		//This does a shallow copy
		return new ArrayList<Property>(this.properties);
	}
	public String[] getKeys(){
    	//Before Java 8
		//This does a shallow copy
        List<String> result = new ArrayList<String>();
        for (Property x : this.properties) {
            result.add(new String(x.getKey()));
        }
    	return result.toArray(new String[]{});
    }
    public Map<String, Property> keyValueMap(){
    	//This does a shallow copy
    	Map<String, Property> result = new HashMap<String, Property>();
    	for (Property parameter : this.properties) {
			result.put(new String(parameter.getKey()), new Property(parameter));
		}
    	return result;
    }
    public Map<String, Object> keyObjectMap(){
    	//This does a shallow copy
    	Map<String, Object> result = new HashMap<String, Object>();
    	for (Property parameter : this.properties) {
			result.put(new String(parameter.getKey()), parameter.getValue());
		}
    	return result;
    }
    public Map<String,Property> keyValueMapToNames(Map<String, String> mappingToFields){ //Key=Column:Name, value=Class.fieldName
    	if(mappingToFields == null || mappingToFields.size() < 1) {
    		return keyValueMap();
    	}
    	//This does a shallow copy
    	Map<String, Property> dataMap = keyValueMap(); 
        Map<String,Property> nXRow = new HashMap<String, Property>(dataMap.size() <= 0 ? 1 : dataMap.size());
        if (dataMap.size() > 0) {
        	//
        	for (Entry<String,String> mapEntry : mappingToFields.entrySet()) {
        		String columnName = mapEntry.getKey();
        		if (dataMap.containsKey(columnName)) {
        			String newKey = mapEntry.getValue();
                	Property m = dataMap.get(columnName);
                	Property newProp = new Property(mapEntry.getValue(), m.getValue(),
                            m.getType());
                    nXRow.put(newKey, newProp);
                }
			}
        }
        return nXRow;
    }
    public int size(){
    	return properties.size();
    }
    public <T> T inflate(Class<T> type) throws InstantiationException, IllegalAccessException {
		return inflate(type, null);
	}
    public <T> T inflate(Class<T> type, Map<String, String> mappingKeys) throws InstantiationException, IllegalAccessException {
		Class<T> cls = type;
		T newInstance = cls.newInstance();
		Field[] fields = cls.getDeclaredFields();
		Map<String, Property> data = this.keyValueMapToNames(mappingKeys);
        for (Field field : fields) {
            field.setAccessible(true);
            Property entry = data.get(field.getName());
            if(entry != null) {
            	field.set(newInstance, entry.getValue());
            }
            field.setAccessible(false);
        }
		return newInstance;
	}
    @Override
    public String toString() {
    	Map<String, Object> objMap = keyObjectMap(); 
    	return objMap.toString();
    }
}
