package ru.armagidon.sit.utils;

import de.Kurfat.Java.Minecraft.BetterChair.PlayerSitEvent;
import de.Kurfat.Java.Minecraft.BetterChair.TypeParseException;
import de.Kurfat.Java.Minecraft.BetterChair.Types.*;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;
import org.spigotmc.event.entity.EntityMountEvent;
import ru.armagidon.sit.SitPluginPlayer;
import ru.armagidon.sit.poses.EnumPose;
import ru.armagidon.sit.poses.sit.ExternalSitPose;

public class BetterChairBridge implements org.bukkit.event.Listener
{

    public BetterChairBridge(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public Chair createChair(Player player){

        Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        Chair chair;
        if (Chair.CACHE_BY_BLOCK.containsKey(block) || Chair.CACHE_BY_PLAYER.containsKey(player) || !block.getRelative(BlockFace.UP).isPassable()){
            return null;
        }
        try {
            chair = new StairChair(player, block);
        } catch (TypeParseException var10) {
            try {
                 chair = new SlapChair(player, block);
            } catch (TypeParseException var9) {
                try {
                     chair = new BedChair(player, block);
                } catch (TypeParseException var8) {
                    try {
                        chair = new SnowChair(player, block);
                    } catch (TypeParseException var7) {
                        try {
                            chair = new CarpetChair(player, block);
                        } catch (TypeParseException var6) {
                            try {
                                chair = new BlockChair(player, block);
                            } catch (TypeParseException var5) {
                                return null;
                            }
                        }
                    }
                }
            }
        }
        return chair;
    }

    @EventHandler
    public void onSit(EntityMountEvent event){
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (Chair.CACHE_BY_PLAYER.containsKey(player)) {
                    Bukkit.getPluginManager().callEvent(new PlayerSitEvent(player, Chair.CACHE_BY_PLAYER.get(player), true));
                }
            }
    }

    @EventHandler
    public void sit(PlayerSitEvent event){
            if(event.isEnable()) {
                SitPluginPlayer p = Listener.players.get(event.getPlayer().getName());
                p.setPose(new ExternalSitPose(event.getPlayer()));
                p.getPlayer().sendMessage(EnumPose.SITTING.getMessage());
            }
    }
}
