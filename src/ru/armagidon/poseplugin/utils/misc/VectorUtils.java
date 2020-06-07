package ru.armagidon.poseplugin.utils.misc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

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

    public static boolean hasIntersection(Vector p1, Vector p2, Vector min, Vector max) {
        final double epsilon = 0.0001f;

        Vector d = p2.clone().subtract(p1).multiply(0.5);
        Vector e = max.clone().subtract(min).multiply(0.5);
        Vector c = p1.clone().add(d).subtract(min.add(max).multiply(0.5));
        Vector ad = absoluteVector(d);

        if (Math.abs(c.getX()) > e.getX() + ad.getX())
            return false;
        if (Math.abs(c.getY()) > e.getY() + ad.getY())
            return false;
        if (Math.abs(c.getZ()) > e.getZ() + ad.getZ())
            return false;

        if (Math.abs(d.getY() * c.getZ() - d.getZ() * c.getY()) > e.getY() * ad.getZ() + e.getZ() * ad.getY() + epsilon)
            return false;
        if (Math.abs(d.getZ() * c.getX() - d.getX() * c.getZ()) > e.getZ() * ad.getX() + e.getX() * ad.getZ() + epsilon)
            return false;
        if (Math.abs(d.getX() * c.getY() - d.getY() * c.getX()) > e.getX() * ad.getY() + e.getY() * ad.getX() + epsilon)
            return false;

        return true;
    }

    private static Vector absoluteVector(Vector v){
        return new Vector(Math.abs(v.getX()),Math.abs(v.getY()),Math.abs(v.getZ()));
    }

}
