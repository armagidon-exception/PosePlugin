package ru.armagidon.poseplugin.api.utils.property;

import java.util.function.Consumer;

public class Property<T>
{
    private final Consumer<T> update;
    private T value;

    public Property(T value, Consumer<T> update) {
        this.value = value;
        this.update = update;
    }

    public T getValue() {
        return value;
    }

    public Class<?> getValueClass(){
        return value.getClass();
    }

    public void setValue(T value) {
        if(value==this.value) return;
        this.value = value;
        update.accept(value);
    }
}
