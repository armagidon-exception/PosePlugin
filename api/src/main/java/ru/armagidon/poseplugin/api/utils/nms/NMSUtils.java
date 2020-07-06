package ru.armagidon.poseplugin.api.utils.nms;


import io.netty.channel.Channel;
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
    public static int getPlayerID(Player player) {
        return (int) getNmsClass("Entity").getDeclaredMethod("getId").invoke(asNMSCopy(player));
    }

    @SneakyThrows
    public static Object createPacketInstance(String name, Class<?>[] types, Object... params) {
        return getNmsClass(name).getConstructor(types).newInstance(params);
    }

    @SneakyThrows
    public static void sendPacket(Player receiver, Object packet){
            Object nmsPlayer = asNMSCopy(receiver);
            Object plrConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            plrConnection.getClass().getMethod("sendPacket", getNmsClass("Packet")).invoke(plrConnection, packet);
    }

    public static Channel getPlayersChannel(Player player) throws IllegalAccessException, NoSuchFieldException {
        Object handle = NMSUtils.asNMSCopy(player);
        Object playerConnection = handle.getClass().getDeclaredField("playerConnection").get(handle);
        Object networkManager = playerConnection.getClass().getDeclaredField("networkManager").get(playerConnection);
        return (Channel) networkManager.getClass().getDeclaredField("channel").get(networkManager);
    }
}
