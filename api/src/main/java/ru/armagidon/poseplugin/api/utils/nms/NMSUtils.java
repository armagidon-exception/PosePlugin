package ru.armagidon.poseplugin.api.utils.nms;


import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

import java.lang.reflect.Method;

import static ru.armagidon.poseplugin.api.utils.nms.ReflectionTools.*;

public class NMSUtils
{

    @SneakyThrows
    public static void setInvisible(Player player, boolean invisible) {
            Object handle = NMSUtils.asNMSCopy(player);
            Method setInvisible = getNmsClass("Entity").getDeclaredMethod("setInvisible",boolean.class);
            setInvisible.setAccessible(true);
            setInvisible.invoke(handle, invisible);
    }

    @SneakyThrows
    public static void setPlayerPose(Player target, Pose pose) {
        Object handle = asNMSCopy(target);
        Class<?> entityClass = getNmsClass("Entity");
        Method setPose = entityClass.getDeclaredMethod("setPose", getNmsClass("EntityPose"));
        setPose.setAccessible(true);

        Enum<?> value = getEnumValues(getEnum("EntityPose"))[pose.ordinal()];
        setPose.invoke(handle, value);
    }

    @SneakyThrows
    public static Object createPosePacket(Player source, boolean dirty){
        Object dataWatcher = getNmsClass("Entity").getDeclaredMethod("getDataWatcher").invoke(asNMSCopy(source));

        return createPacketInstance("PacketPlayOutEntityMetadata", new Class[]{int.class, getNmsClass("DataWatcher"), boolean.class}, source.getEntityId(), dataWatcher, dirty);
    }

    @SneakyThrows
    public static Object asNMSCopy(Player player) {
        Method m = player.getClass().getDeclaredMethod("getHandle");
        m.setAccessible(true);
        return m.invoke(player);
    }

    @SneakyThrows
    public static Object createPacketInstance(String name, Class<?>[] types, Object... params) {
        return getNmsClass(name).getConstructor(types).newInstance(params);
    }

    @SneakyThrows
    public static void sendPacket(Player receiver, Object packet) {
        Object nmsPlayer = asNMSCopy(receiver);
        Object plrConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
        plrConnection.getClass().getMethod("sendPacket", getNmsClass("Packet")).invoke(plrConnection, packet);
    }

    @SneakyThrows
    public static Object createPosePacket(Player target, Pose pose){
        Object dataWatcher = getNmsClass("Entity").getDeclaredMethod("getDataWatcher").invoke(asNMSCopy(target));
        //DataWatcherRegistry.s.a(int)
        Object serializer = getNmsClass("DataWatcherRegistry").getDeclaredField("s").get(null);

        Object entityPose = getEnumValues(getEnum("EntityPose"))[pose.ordinal()];

        Object datawatcherObject = getNmsClass("DataWatcherSerializer").getDeclaredMethod("a",int.class).invoke(serializer, 6);

        dataWatcher.getClass().getDeclaredMethod("set",getNmsClass("DataWatcherObject"),Object.class).invoke(dataWatcher, datawatcherObject, entityPose);

        return createPacketInstance("PacketPlayOutEntityMetadata", new Class[]{int.class, getNmsClass("DataWatcher"), boolean.class}, target.getEntityId(), dataWatcher, true);
    }
}
