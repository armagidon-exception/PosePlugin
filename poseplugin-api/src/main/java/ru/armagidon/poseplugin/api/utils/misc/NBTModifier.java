package ru.armagidon.poseplugin.api.utils.misc;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

import static ru.armagidon.poseplugin.api.utils.nms.ReflectionTools.getCBClass;
import static ru.armagidon.poseplugin.api.utils.nms.ReflectionTools.getNmsClass;

public class NBTModifier
{

    private static final String COMPOUND_CLASS_NAME = "NBTTagCompound";

    private static Method AS_BUKKIT;
    private static Method AS_NMS;
    private static Method GET_TAG;
    private static Method SET_STRING;
    private static Method GET_STRING;
    private static Method HAS_KEY;
    private static Method REMOVE_TAG;

    static {
        try {
            AS_BUKKIT = getMethodSafely(getCBClass("inventory.CraftItemStack"), "asBukkitCopy", getNmsClass("ItemStack"));
            AS_NMS = getMethodSafely(getCBClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class);
            GET_TAG = getMethodSafely(getNmsClass("ItemStack"), "getOrCreateTag");
            SET_STRING = getMethodSafely(getNmsClass(COMPOUND_CLASS_NAME), "setString", String.class, String.class);
            GET_STRING = getMethodSafely(getNmsClass(COMPOUND_CLASS_NAME), "getString", String.class);
            REMOVE_TAG = getMethodSafely(getNmsClass(COMPOUND_CLASS_NAME), "remove", String.class);
            HAS_KEY = getMethodSafely(getNmsClass(COMPOUND_CLASS_NAME), "hasKey", String.class);
        } catch (Exception e){
            AS_BUKKIT = getMethodSafely(getCBClass("inventory.CraftItemStack"), "asBukkitCopy", getNmsClass("ItemStack"));
            AS_NMS = getMethodSafely(getCBClass("inventory.CraftItemStack"), "asNMSCopy", ItemStack.class);
            GET_TAG = getMethodSafely(getNmsClass("world.item.ItemStack"), "getOrCreateTag");
            SET_STRING = getMethodSafely(getNmsClass("nbt."+COMPOUND_CLASS_NAME), "setString", String.class, String.class);
            GET_STRING = getMethodSafely(getNmsClass("nbt."+COMPOUND_CLASS_NAME), "getString", String.class);
            REMOVE_TAG = getMethodSafely(getNmsClass("nbt."+COMPOUND_CLASS_NAME), "remove", String.class);
            HAS_KEY = getMethodSafely(getNmsClass("nbt."+COMPOUND_CLASS_NAME), "hasKey", String.class);
        }

    }

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
