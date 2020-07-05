package ru.armagidon.poseplugin.api.utils.property;

import com.google.common.collect.Maps;

import java.util.Map;

public class PropertyMap
{
    private boolean registered;

    @SuppressWarnings("raw")
    private Map<String, Property> propertyMap = Maps.newHashMap();

    public <T> void registerProperty(String key, Property<T> property) {
        if(isRegistered()){
            throw new IllegalStateException("Properties are registered, you can't register it anymore!");
        }
        if(key.equals("") || key.equals(" ")){
            throw new IllegalArgumentException("Key cannot be empty!");
        }
        if(propertyMap.containsKey(key)){
            throw new IllegalArgumentException("This property is already registered! You can only modify it!");
        }
        if(property==null){
            throw new IllegalArgumentException("Property object cannot be null!");
        }
        propertyMap.put(key, property);
    }

    @SuppressWarnings("unchecked")
    public <T> Property<T> getProperty(String key, Class<T> type){
        if(!propertyMap.containsKey(key)) return null;
        if(propertyMap.get(key)==null) return null;
        if(propertyMap.get(key).getValue()==null) return null;
        Property<T> property = propertyMap.get(key);
        Class<?> clazz = property.getValueClass();
        if(!type.equals(clazz)){
            throw new IllegalArgumentException("Class "+type.getTypeName()+ " doesn't match to "+clazz);
        }
        return propertyMap.get(key);
    }

    public void register(){
        this.registered = true;
    }

    private boolean isRegistered() {
        return registered;
    }
}
