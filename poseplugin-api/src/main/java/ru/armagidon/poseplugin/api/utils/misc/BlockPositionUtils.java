package ru.armagidon.poseplugin.api.utils.misc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class BlockPositionUtils
{
    public static Block getDirBlock(Location playerLocation){
        Block cur = getBlockOnLoc(playerLocation);
        BlockFace face = yawToFace(playerLocation.getYaw());
        if(face == null) return cur;
        return cur.getRelative(face);
    }

    public static final BlockFace[] axis = { BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST };

    public static BlockFace yawToFace(float yaw) {
        return axis[Math.round(yaw / 90f) & 0x3];
    }

    public static Set<Player> getNear(double radius, Player player) {
        Set<Player> players = new HashSet<>();
        Bukkit.getOnlinePlayers().stream().filter((other) -> player.getWorld().equals(other.getWorld())).
                filter((other) -> player.getLocation().distance(other.getLocation()) <= radius).forEach(players::add);
        return players;
    }

    public static Set<Player> getNearSquared(int radius, Player source){
        return Bukkit.getOnlinePlayers().stream().filter((other) -> source.getWorld().equals(other.getWorld())).
                filter((other) -> source.getLocation().distanceSquared(other.getLocation()) <= radius).collect(Collectors.toSet());
    }

    public static Block getRoundedBlock(Location location){
        int x = location.getBlockX();
        int z = location.getBlockZ();
        int y = round(location.getY());
        return Objects.requireNonNull(location.getWorld()).getBlockAt(x,y,z);
    }

    public static Block getBlockOnLoc(Location location){
        int x = (int) location.getX();
        int y = NumberConversions.ceil(location.getY());
        int z = (int) location.getZ();
        return location.getWorld().getBlockAt(x,y,z);
    }

    public static boolean onGround(Player player){
        Location location = player.getLocation();
        return !getBelow(location).getType().isAir() && player.isOnGround();
    }

    public static int round(double source){
        final float rounder = 0.5F;
        final int stripped_value = (int) source;
        final int incremented_stripped_value = stripped_value + 1;

        return source + rounder >= incremented_stripped_value ? incremented_stripped_value : stripped_value;
    }

    private static boolean isFraction(double num) {
        int i = (int) num;
        double d = Math.ceil(num);
        return i < d;
    }

    public static Block getBelow(Location location) {
        if (isFraction(location.getY())) {
            return location.getBlock();
        } else {
            return location.getBlock().getRelative(BlockFace.DOWN);
        }
    }

}
