package ru.armagidon.poseplugin.api.utils.misc;

import lombok.Getter;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DataTable<KeyType, ValueType>
{

    private final Map<KeyType, DataObject<KeyType, ValueType>> table = new HashMap<>();

    private boolean dirty = false;

    public void define(KeyType key, ValueType defaultValue) {
        if (table.containsKey(key))
            throw new IllegalArgumentException("This key is already defined in this table");
        DataObject<KeyType, ValueType> object = new DataObject<>(key, defaultValue);
        table.put(key, object);
    }

    public void set(@NotNull KeyType key, @NotNull ValueType value) {
        Validate.notNull(key);
        Validate.notNull(value);

        if (!table.containsKey(key))
            throw new IllegalArgumentException("This key is not defined in the table");

        DataObject<KeyType, ValueType> object = table.get(key);
        object.setValue(value);

        dirty = true;
    }

    @Nullable
    public ValueType get(@NotNull KeyType key) {
        Validate.notNull(key);
        if (!table.containsKey(key))
            return null;
        return table.get(key).getValue();
    }

    public int dirtyCount() {
        return (int) table.values().stream().
                filter(DataObject::isDirty).count();
    }

    @NotNull
    public Collection<DataObject<KeyType, ValueType>> getAll() {
        return table.values();
    }

    @NotNull
    public Collection<DataObject<KeyType, ValueType>> getDirty() {
        if (!dirty) return Collections.emptyList();
        var list = getAll().stream()
                .filter(DataObject::isDirty)
                .peek(DataObject::clean)
                .collect(Collectors.toSet());
        dirty = false;
        return list;
    }


    public static class DataObject<K, V> {
        @Getter private final K key;
        @Getter private V value;
        @Getter private boolean dirty = false; //Changed?

        public DataObject(@NotNull K key, @NotNull V value) {
            this.key = key;
            this.value = value;
        }

        public void setValue(@NotNull V value) {
            if (value.equals(this.value)) return;
            this.value = value;
            this.dirty = true;
        }

        public void clean() {
            this.dirty = false;
        }

        @Override
        public String toString() {
            return "DataObject{" +
                    "key=" + key +
                    ", value=" + value +
                    ", dirty=" + dirty +
                    '}';
        }
    }
}
