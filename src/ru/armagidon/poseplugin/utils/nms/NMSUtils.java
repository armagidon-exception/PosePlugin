package ru.armagidon.poseplugin.utils.nms;

import net.minecraft.server.v1_15_R1.Packet;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.utils.nms.impl._1_15.DamagePacketReader;
import ru.armagidon.poseplugin.utils.nms.impl._1_15.FakePlayer_1_15;
import ru.armagidon.poseplugin.utils.nms.interfaces.FakePlayer;
import ru.armagidon.poseplugin.utils.nms.interfaces.PacketReader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NMSUtils
{

    private SpigotVersion version;

    public NMSUtils() {
        version = SpigotVersion.currentVersion();
    }

    public enum SpigotVersion {
        VERSION_UNKNOWN,
        VERSION_1_14,
        VERSION_1_15;

        SpigotVersion() {}

        public static SpigotVersion currentVersion() {
            String version = Bukkit.getVersion();
            if (!version.endsWith("(MC: 1.15)") && !version.endsWith("(MC: 1.15.1)") && !version.endsWith("(MC: 1.15.2)")) {
                return !version.endsWith("(MC: 1.14)") && !version.endsWith("(MC: 1.14.1)") && !version.endsWith("(MC: 1.14.2)") && !version.endsWith("(MC: 1.14.3)") && !version.endsWith("(MC: 1.14.4)") ? VERSION_UNKNOWN : VERSION_1_14;
            } else {
                return VERSION_1_15;
            }
        }

        public static List<SpigotVersion> compatibleVersions() {
            List<SpigotVersion> versions = new ArrayList<>();
            SpigotVersion version = currentVersion();
            if (version == VERSION_UNKNOWN) {
                return versions;
            } else {
                versions.add(VERSION_1_14);
                if (version == VERSION_1_14) {
                    return versions;
                } else {
                    versions.add(VERSION_1_15);
                    return versions;
                }
            }
        }
    }

    public static FakePlayer getFakePlayerInstance(Player parent){
        switch (SpigotVersion.currentVersion()){
            case VERSION_1_15:
                return new FakePlayer_1_15(parent);
            default:
                throw new RuntimeException("Unsupported version");
        }
    }

    public static PacketReader getDamageReader(Player player){
        return new DamagePacketReader(player);
    }

    public static Object getValue(Object source, String field) {
        try {
            Field f = source.getClass().getDeclaredField(field);
            f.setAccessible(true);
            return f.get(source);
        } catch (Exception e) {
            return null;
        }
    }

    public static void set(Object source, Object value, String field) throws Exception{
            Field f = source.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(source, value);
    }

    public static void sendPacket(Player receiver, Object packet) {
        try {
            Object nmsPlayer = receiver.getClass().getMethod("getHandle").invoke(receiver);
            Object plrConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            plrConnection.getClass().getMethod("sendPacket", getNmsClass("Packet")).invoke(plrConnection, packet);
        } catch (Exception e){
            PosePlugin.getInstance().getLogger().severe(e.toString());
        }
    }

    public static void sendPacket(Player receiver, Packet<?> packet){
        ((CraftPlayer)receiver).getHandle().playerConnection.sendPacket(packet);
    }

    public static Class<?> getNmsClass(String nmsClassName) throws Exception {
        return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + nmsClassName);
    }

    public static Object createNMSObject(String packetname, Class<?>[] types, Object... args) throws Exception{
        return getNmsClass(packetname).getConstructor(types).newInstance(args);
    }

    public static Method getHandle(Object source) throws Exception {
        return getMethod(source,"getHandle");
    }

    public static Method getMethod(Object source, String name, Class<?>... types) throws Exception {
        Method method = source.getClass().getDeclaredMethod(name,types);
        method.setAccessible(true);
        return method;
    }

    public static Class<Enum> getEnumClass(String name) throws Exception {
        return (Class<Enum>) getNmsClass(name);
    }

    public static Class<Enum> getNestedEnum(String owner, String EnumName) throws Exception {
        Class<?>[] classes = getNmsClass(owner).getDeclaredClasses();
        for (Class<?> clazz : classes) {
            if(clazz.getName().equalsIgnoreCase(EnumName)){
                return (Class<Enum>) clazz;
            }
        }
        throw new ClassNotFoundException("Enum "+EnumName+" was not found in this class "+owner);
    }

}
