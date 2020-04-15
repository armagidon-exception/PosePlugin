package ru.armagidon.sit.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class VectorUtils
{

    //west - 45-135
    //north - (-135)-135
    //east - (-135)-(-45)
    //south - (-45)-45

    public static Block getDirBlock(Location plocation){
        Block cur = plocation.getBlock();
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
            case EAST:
                return 0;
            case WEST:
                return 300;
            case NORTH:
                return 100;
            case SOUTH:
                return 90;
            default:
                return 0;
        }
    }

}
