package ru.armagidon.poseplugin.utils.nms;

import net.minecraft.server.v1_15_R1.Packet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.PosePlugin;

import java.lang.reflect.Field;

public class NMSUtils
{

    public static void sendPacket(Player receiver, Packet<?> packet) {
        try {
            Object nmsPlayer = receiver.getClass().getMethod("getHandle").invoke(receiver);
            Object plrConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            plrConnection.getClass().getMethod("sendPacket", getNmsClass("Packet")).invoke(plrConnection, packet);
        } catch (Exception e){
            PosePlugin.getInstance().getLogger().severe(e.toString());
        }
    }

    private static Class<?> getNmsClass(String nmsClassName) throws Exception {
        return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + nmsClassName);
    }

    static void setField(String name, Object source, Object value, Class<?> clazz){
        try {
            Field dW = clazz.getDeclaredField(name);
            dW.setAccessible(true);
            dW.set(source, value);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setInvisible(Player player, boolean invisible){
        FakePlayer.asNMSCopy(player).setInvisible(invisible);
    }

}
