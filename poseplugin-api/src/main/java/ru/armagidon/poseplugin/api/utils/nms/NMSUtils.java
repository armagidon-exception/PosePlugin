package ru.armagidon.poseplugin.api.utils.nms;


import lombok.SneakyThrows;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import static ru.armagidon.poseplugin.api.utils.nms.ReflectionTools.*;

public class NMSUtils
{

    @SneakyThrows
    public static void setAABB(Entity target, Object aabb) {
        Object handle = asNMSCopy(target);
        setAABB(handle, aabb);
    }

    @SneakyThrows
    public static void setAABB(Object handle, Object aabb) {
        Method setBoundingBox = getNmsClass("Entity").getDeclaredMethod("a", getNmsClass("AxisAlignedBB"));
        setBoundingBox.invoke(handle, aabb);
    }

    @SneakyThrows
    public static Object getSwimmingAABB(Player player) {
        Object handle = asNMSCopy(player);
        Method poseToAABB = getNmsClass("EntityLiving").getDeclaredMethod("f", getNmsClass("EntityPose"));
        Enum<?> pose = getEnumValues(getEnum("EntityPose"))[Pose.SWIMMING.ordinal()];
        return poseToAABB.invoke(handle, pose);
    }

    @SneakyThrows
    public static void setInvisible(Object handle, boolean invisible) {
        Method setInvisible = getNmsClass("Entity").getDeclaredMethod("setInvisible",boolean.class);
        setInvisible.setAccessible(true);
        setInvisible.invoke(handle, invisible);
    }

    @SneakyThrows
    public static Object asNMSCopy(Entity e) {
        Method m = e.getClass().getDeclaredMethod("getHandle");
        m.setAccessible(true);
        return m.invoke(e);
    }

    @SneakyThrows
    public static Object createPacketInstance(String name, Class<?>[] types, Object... params) {
        return getNmsClass(name).getConstructor(types).newInstance(params);
    }

    @SneakyThrows
    public static void sendPacket(Player receiver, Object packet) {
        Object nmsPlayer = asNMSCopy(receiver);
        Object plrConnection = nmsPlayer.getClass().getDeclaredField("playerConnection").get(nmsPlayer);
        plrConnection.getClass().getMethod("sendPacket", getNmsClass("Packet")).invoke(plrConnection, packet);
    }

    @SneakyThrows
    public static Object createPosePacket(Player target, Pose pose){
        Object dataWatcher = getNmsClass("Entity").getDeclaredMethod("getDataWatcher").invoke(asNMSCopy(target));

        Object entityPose = getEnumValues(getEnum("EntityPose"))[pose.ordinal()];

        Object datawatcherObject = getDataWatcherObject("s", 6);

        dataWatcher.getClass().getDeclaredMethod("set",getNmsClass("DataWatcherObject"),Object.class).invoke(dataWatcher, datawatcherObject, entityPose);

        return createPacketInstance("PacketPlayOutEntityMetadata", new Class[]{int.class, getNmsClass("DataWatcher"), boolean.class}, target.getEntityId(), dataWatcher, true);
    }

    @SneakyThrows
    public static void setValueToDW(Object dataWatcher, Object dwObj, Object value) {
        Method method = dataWatcher.getClass().getDeclaredMethod("set", getNmsClass("DataWatcherObject"), Object.class);
        method.setAccessible(true);
        method.invoke(dataWatcher, dwObj, value);
    }

    @SneakyThrows
    public static Object getStaticDWObjectFromEntity(String fieldName, Object entity) {
        Field f = entity.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(null);
    }

    @SneakyThrows
    public static Object getDataWatcher(Object entity){
        return getNmsClass("Entity").getDeclaredMethod("getDataWatcher").invoke(entity);
    }

    @SneakyThrows
    private static Object getSerializer(String fieldName){
        return getNmsClass("DataWatcherRegistry").getDeclaredField(fieldName).get(null);
    }

    @SneakyThrows
    public static Object getDataWatcherObject(String fieldName, int index){
        return getNmsClass("DataWatcherSerializer").getDeclaredMethod("a",int.class).invoke(getSerializer(fieldName), index);
    }

    @SneakyThrows
    public static Object createBoundingBox(double d, double d1, double d2, double d3, double d4, double d5) {
        Constructor<?> constructor = getNmsClass("AxisAlignedBB").getDeclaredConstructor(double.class, double.class, double.class, double.class, double.class, double.class);
        constructor.setAccessible(true);
        return constructor.newInstance(d, d1, d2, d3, d4, d5);
    }
}
