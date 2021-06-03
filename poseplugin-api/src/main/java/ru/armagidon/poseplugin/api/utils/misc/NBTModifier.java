package ru.armagidon.poseplugin.api.utils.misc;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class NBTModifier
{

    private static final String COMPOUND_CLASS_NAME = "NBTTagCompound";

    private static final Method AS_BUKKIT = getMethodSafely(getCBClass("inventory.CraftItemStack"), "asBukkitCopy", getNmsClass("ItemStack"));
    private static final Method AS_NMS = getMethodSafely(getCBClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class);
    private static final Method GET_TAG = getMethodSafely(getNmsClass("ItemStack"), "getOrCreateTag");
    private static final Method SET_STRING = getMethodSafely(getNmsClass(COMPOUND_CLASS_NAME), "setString", String.class, String.class);
    private static final Method GET_STRING = getMethodSafely(getNmsClass(COMPOUND_CLASS_NAME), "getString", String.class);
    private static final Method REMOVE_TAG = getMethodSafely(getNmsClass(COMPOUND_CLASS_NAME), "remove", String.class);
    private static final Method HAS_KEY = getMethodSafely(getNmsClass(COMPOUND_CLASS_NAME), "hasKey", String.class);

    @SneakyThrows
    public static void setString(ItemStack stack, String name, String value) {
        ItemStack modifier = stack.clone();
        Object originalStack = asNMS(modifier);
        Object compound = getTag(originalStack);
        SET_STRING.invoke(compound, name, value);
        modifier = (ItemStack) AS_BUKKIT.invoke(null, originalStack);

        stack.setItemMeta(modifier.getItemMeta());
    }

    @SneakyThrows
    public static String getString(ItemStack stack, String name){
        Object originalStack = asNMS(stack);
        Object compound = getTag(originalStack);
        return (String) GET_STRING.invoke(compound, name);
    }


    /**
     * @param stack - item which tag will be removed from.
     * @param name - tag name to remove from {@code stack}
     * @implNote if item does not have tag {@code name} then method will be returned
     * */
    @SneakyThrows
    public static void remove(ItemStack stack, String name){
        if (!hasTag(stack, name)) return;
        ItemStack modifier = stack.clone();
        Object originalStack = asNMS(modifier);
        Object compound = getTag(originalStack);
        REMOVE_TAG.invoke(compound, name);
        modifier = (ItemStack) AS_BUKKIT.invoke(null, originalStack);
        stack.setItemMeta(modifier.getItemMeta());
    }

    @SneakyThrows
    public static boolean hasTag(ItemStack stack, String name){
        Object original = asNMS(stack);
        Object compound = getTag(original);
        return (boolean) HAS_KEY.invoke(compound, name);
    }

    @SneakyThrows
    private static Object getTag(Object stack){
        return GET_TAG.invoke(stack);
    }

    @SneakyThrows
    private static Object asNMS(ItemStack source){
        return AS_NMS.invoke(null, source);
    }

    @SneakyThrows
    private static Class<?> getCBClass(String craftBukkitClassName){
        return Class.forName("org.bukkit.craftbukkit." +nmsVersion() + "." + craftBukkitClassName);
    }

    @SneakyThrows
    private static Class<?> getNmsClass(String nmsClassName)  {
        return Class.forName("net.minecraft.server." +nmsVersion() + "." + nmsClassName);
    }

    public static String nmsVersion(){
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    private static <E extends Enum<?>> E[] getEnumValues(Class<E> enumClass) {
        return enumClass.getEnumConstants();
    }

    @SuppressWarnings("unchecked")
    private static Class<Enum<?>> getEnum(String name) {
        return (Class<Enum<?>>) getNmsClass(name);
    }

    @SneakyThrows
    private static Method getMethodSafely(Class<?> clazz, String name, Class<?>... argTypes){
        return clazz.getDeclaredMethod(name, argTypes);
    }
}
