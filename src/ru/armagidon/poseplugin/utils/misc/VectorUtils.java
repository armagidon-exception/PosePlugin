package ru.armagidon.poseplugin.utils.misc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.ArrayList;
import java.util.List;

public final class VectorUtils
{
    public static Block getDirBlock(Location plocation){
        Block cur = getBlock(plocation);
        BlockFace face = yawToFace(plocation.getYaw());
        if(face==null) return null;
        return cur.getRelative(face);
    }

    public static final BlockFace[] axis = { BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST };

    public static BlockFace yawToFace(float yaw) {
        return axis[Math.round(yaw / 90f) & 0x3];
    }

    public static List<Player> getNear(double radius, Player player) {
        List<Player> players = new ArrayList<>();
        Bukkit.getOnlinePlayers().stream().filter((other) -> player.getWorld().equals(other.getWorld())).filter((other) -> player.getLocation().distance(other.getLocation()) <= radius).forEach(players::add);
        return players;
    }

    public static float faceToYaw(BlockFace face){
        switch (face){
            case WEST:
                return 0;
            case EAST:
                return 132;
            case NORTH:
                return 50;
            case SOUTH:
                return 290;
            default:
                return 0;
        }
    }
    public static Block getBlock(Location location){
        int x = location.getBlockX();
        int z = location.getBlockZ();
        int y = NumberConversions.ceil(location.getY());
        return location.getWorld().getBlockAt(x,y,z);
    }

}
