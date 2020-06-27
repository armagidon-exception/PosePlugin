package ru.armagidon.poseplugin.utils.nms;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionTools
{

    public static Class<?> getNmsClass(String nmsClassName) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." +nmsVersion() + "." + nmsClassName);
    }

    public static String nmsVersion(){
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    public static <E extends Enum> E[] getEnumValues(Class<E> enumClass) {
        return enumClass.getEnumConstants();
    }

    @SuppressWarnings("unchecked")
    public static Class<Enum<?>> getEnum(String name) throws ClassNotFoundException {
        return (Class<Enum<?>>) getNmsClass(name);
    }

    public static Plugin getPlugin(){
        try {
            Class<?> c = Class.forName("ru.armagidon.poseplugin.PosePlugin");
            Method m = c.getDeclaredMethod("getInstance");
            return (Plugin) m.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException |  IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}
