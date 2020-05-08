package ru.armagidon.sit.poses;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import static org.bukkit.Material.*;
import static ru.armagidon.sit.utils.ConfigurationManager.*;

public class SwimPose extends PluginPose
{

    private Location cache = null;

    public SwimPose(Player player) {
        super(player);
    }

    @Override
    public void play(Player receiver, boolean log) {
        super.play(receiver,log);
        getPlayer().setCollidable((Boolean) get(COLLIDABLE));
        Block above = getPlayer().getLocation().getBlock().getRelative(BlockFace.UP);
        if(isAir(above.getType())){
            placeBlockAbove(above);
        }
    }

    @Override
    public void stop(boolean log) {
        super.stop(log);
        if(cache!=null) {
            restoreBlock();
        }
        getPlayer().setCollidable(true);
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.SWIM;
    }


    @EventHandler
    public void move(PlayerMoveEvent event) {
        if(!containsPlayer(event.getPlayer())) return;
        if(event.getPlayer().getName().equalsIgnoreCase(getPlayer().getName())) {
            Block above = getPlayer().getLocation().getBlock().getRelative(BlockFace.UP);
            if (cache != null) {
                restoreBlock();
            }
            if (isAir(above.getType())) {
                placeBlockAbove(above);
            }
        } else {
            Player another = event.getPlayer();
            Location under = another.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();
            if(cache!=null&&cache.equals(under)){
                stop(false);
                getPlayer().sendMessage((String) get(BREAK_BACK));
            }
        }
    }

    private void restoreBlock(){
        cache.getBlock().setType(AIR);
        cache = null;
    }

    private void placeBlockAbove(Block above){
        above.setType(BARRIER);
        cache = above.getLocation();
    }

    private boolean isAir(Material mat){
        return mat.equals(AIR)||mat.equals(VOID_AIR)||mat.equals(CAVE_AIR);
    }
}
