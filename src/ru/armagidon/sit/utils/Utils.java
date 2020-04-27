package ru.armagidon.sit.utils;

import net.minecraft.server.v1_15_R1.Packet;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.armagidon.sit.SitPlugin;
import ru.armagidon.sit.SitPluginPlayer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Utils
{


    public static FileConfiguration configuration = SitPlugin.getPlugin(SitPlugin.class).getConfig();
    public static final Map<String, SitPluginPlayer> players = new HashMap<>();
    public static final String AIR = configuration.getString("in-air","in-air");
    public static final boolean SWIM_ENABLED =  configuration.getBoolean("swim-animation.enabled");
    public static final boolean COLLIDABLE = configuration.getBoolean("collidable",true);
    public static final boolean CHECK_FOR_UPDATED = configuration.getBoolean("check-for-updates",true);

    public static void setValue(String name, Object value, Object obj){
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e){
            System.out.println(e);
        }
    }

    public static void sendPacket(Player player, Packet<?> packet){
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }

    public static void sendPacket(Player player, net.minecraft.server.v1_14_R1.Packet<?> packet){
        ((org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
    }


}