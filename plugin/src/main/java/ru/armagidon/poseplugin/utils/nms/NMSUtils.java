package ru.armagidon.poseplugin.utils.nms;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static ru.armagidon.poseplugin.utils.nms.ReflectionTools.*;

public class NMSUtils
{

    public static void setInvisible(Player player, boolean invisible) {
        try {
            Object handle = NMSUtils.asNMSCopy(player);
            Method setInvisible = getNmsClass("Entity").getDeclaredMethod("setInvisible",boolean.class);
            setInvisible.setAccessible(true);
            setInvisible.invoke(handle, invisible);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
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

    public static Object asNMSCopy(Player player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            Method m = player.getClass().getDeclaredMethod("getHandle");
            m.setAccessible(true);
            return m.invoke(player);
    }

    public static int getPlayerID(Player player) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        return (int) getNmsClass("Entity").getDeclaredMethod("getId").invoke(asNMSCopy(player));
    }

    public static Object createPacketInstance(String name, Class[] types, Object... params) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return getNmsClass(name).getConstructor(types).newInstance(params);
    }

    public static void sendPacket(Player receiver, Object packet){
        try {
            Object nmsPlayer = asNMSCopy(receiver);
            Object plrConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            plrConnection.getClass().getMethod("sendPacket", getNmsClass("Packet")).invoke(plrConnection, packet);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Channel getPlayersChannel(Player player) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
            Object handle = NMSUtils.asNMSCopy(player);
            Object playerConnection = handle.getClass().getDeclaredField("playerConnection").get(handle);
            Object networkManager = playerConnection.getClass().getDeclaredField("networkManager").get(playerConnection);

        return (Channel) networkManager.getClass().getDeclaredField("channel").get(networkManager);
    }
}
