package ru.armagidon.poseplugin.api.utils.nms;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;

public class ReflectionTools
{

    @SneakyThrows
    public static Class<?> getNmsClass(String nmsClassName)  {
        return Class.forName("net.minecraft.server." +nmsVersion() + "." + nmsClassName);
    }

    public static String nmsVersion(){
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    public static <E extends Enum<?>> E[] getEnumValues(Class<E> enumClass) {
        return enumClass.getEnumConstants();
    }

    @SuppressWarnings("unchecked")
    public static Class<Enum<?>> getEnum(String name) {
        return (Class<Enum<?>>) getNmsClass(name);
    }
}
