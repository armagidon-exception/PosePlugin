package ru.armagidon.poseplugin.utils.nms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSUtils
{

    public static void sendPacket(Player receiver, Object packet){
        try {
            Object nmsPlayer = asNMSCopy(receiver);
            Object plrConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            plrConnection.getClass().getMethod("sendPacket", getNmsClass("Packet")).invoke(plrConnection, packet);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Class<?> getNmsClass(String nmsClassName) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + nmsClassName);
    }

    public static void setInvisible(Player player, boolean invisible) {
        try {
            Object handle = NMSUtils.asNMSCopy(player);
            Method setInvisible = handle.getClass().getDeclaredMethod("setInvisible",boolean.class);
            setInvisible.setAccessible(true);
            setInvisible.invoke(handle, invisible);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void setPlayerPose(Player target, Pose pose) {
        try {
            Object handle = asNMSCopy(target);
            Class<?> entityClass = getNmsClass("Entity");
            Method setPose = entityClass.getDeclaredMethod("setPose", getNmsClass("EntityPose"));
            setPose.setAccessible(true);

            Enum<?> value = getEnumValues(getEnum("EntityPose"))[pose.ordinal()];
            setPose.invoke(handle, value);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum> E[] getEnumValues(Class<E> enumClass) {
        return enumClass.getEnumConstants();
    }

    public static Object asNMSCopy(Player player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = player.getClass().getDeclaredMethod("getHandle");
        m.setAccessible(true);
        return m.invoke(player);
    }

    @SuppressWarnings("unchecked")
    public static Class<Enum<?>> getEnum(String name) throws ClassNotFoundException {
        return (Class<Enum<?>>) getNmsClass(name);
    }

    public static int getPlayerID(Player player) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        return (int) getNmsClass("Entity").getDeclaredMethod("getId").invoke(asNMSCopy(player));
    }

    public static Object createPacketInstance(String name, Class[] types, Object... params) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return getNmsClass(name).getConstructor(types).newInstance(params);
    }

}
