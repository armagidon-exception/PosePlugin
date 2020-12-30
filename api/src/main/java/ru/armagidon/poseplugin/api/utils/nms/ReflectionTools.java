package ru.armagidon.poseplugin.api.utils.nms;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;

public class ReflectionTools
{

    public static Class<?> getNmsClass(String nmsClassName) throws Exception {
        return Class.forName("net.minecraft.server." +nmsVersion() + "." + nmsClassName);
    }

    public static String nmsVersion(){
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    public static <E extends Enum<?>> E[] getEnumValues(Class<E> enumClass) {
        return enumClass.getEnumConstants();
    }

    @SuppressWarnings("unchecked")
    public static Class<Enum<?>> getEnum(String name) throws Exception {
        return (Class<Enum<?>>) getNmsClass(name);
    }

    @SuppressWarnings("unchecked")
    public static Class<Enum<?>> getNestedEnum(Class<?> owner, String name) throws ClassNotFoundException {
        return (Class<Enum<?>>) getNestedClass(owner, name);
    }

    public static Class<?> getNestedClass(Class<?> owner, String name) throws ClassNotFoundException {
        String path = String.format("%s$%s", owner.getTypeName(), name);
        return Class.forName(path);
    }

    @SneakyThrows
    public static Method getMethodSafely(Class<?> clazz, String name, Class<?>... argTypes){
        return clazz.getDeclaredMethod(name, argTypes);
    }
}
