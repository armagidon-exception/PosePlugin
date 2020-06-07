package ru.armagidon.poseplugin.utils.nms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.PosePlugin;

public class NMSUtils {
    public NMSUtils() {
    }

    public static PacketReader getSwingReader(Player player) {
        return new SwingPacketReader(player);
    }

    public static void sendPacket(Player receiver, Object packet) {
        try {
            Object nmsPlayer = receiver.getClass().getMethod("getHandle").invoke(receiver);
            Object plrConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            plrConnection.getClass().getMethod("sendPacket", getNmsClass("Packet")).invoke(plrConnection, packet);
        } catch (Exception e) {
            PosePlugin.getInstance().getLogger().severe(e.toString());
        }
    }

    public static Class<?> getNmsClass(String nmsClassName) throws Exception {
        return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + nmsClassName);
    }

}
