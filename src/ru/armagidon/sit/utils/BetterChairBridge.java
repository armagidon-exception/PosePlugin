package ru.armagidon.sit.utils;

import de.Kurfat.Java.Minecraft.BetterChair.TypeParseException;
import de.Kurfat.Java.Minecraft.BetterChair.Types.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class BetterChairBridge
{

    private final boolean status;

    public BetterChairBridge(Plugin plugin) {
        this.status = plugin.getServer().getPluginManager().getPlugin("BetterChair")!=null;
    }

    public boolean isActivated() {
        return status;
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
}
