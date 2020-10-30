package ru.armagidon.poseplugin.api.utils.property;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.function.BiConsumer;

public class PropertyMap
{
    private boolean registered;

    private final Map<String, Property<?>> propertyMap = Maps.newHashMap();

    public <T> PropertyMap registerProperty(String key, Property<T> property) {
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
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Property<T> getProperty(String key, Class<T> type){
        if(!propertyMap.containsKey(key)) throw new IllegalArgumentException("This property is not registered");
        if(propertyMap.get(key) == null) throw new NullPointerException("Property is null");
        if(propertyMap.get(key).getValue()==null) throw new NullPointerException("Value of this property is null");
        Property<?> property = propertyMap.get(key);
        Class<?> clazz = property.getValueClass();
        if(!type.equals(clazz)){
            throw new IllegalArgumentException("Class "+type.getTypeName()+ " doesn't match to "+clazz);
        }
        return (Property<T>) property;
    }

    public void register(){
        this.registered = true;
    }

    private boolean isRegistered() {
        return registered;
    }

    public void forEach(BiConsumer<String,Property<?>> action){
        propertyMap.forEach(action);
    }
}
