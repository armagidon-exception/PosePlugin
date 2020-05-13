package ru.armagidon.poseplugin.poses.sit;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class InternalSitPose extends SitPose
{

    private ArmorStand seat;
    private Block block;
    
    public InternalSitPose(Player player) {
        super(player);
        this.block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
    }

    @Override
    public void standUp(Player player) {
        seat.remove();
    }

    @Override
    public void takeASeat(Player player, Location l) {
        Location location = l.clone();
        seat = player.getWorld().spawn(location.clone().subtract(0, 1.7, 0), ArmorStand.class);
        seat.setVisible(false);
        seat.setGravity(false);
        seat.addPassenger(player);
    }

    @EventHandler
    public void armorManipulate(PlayerArmorStandManipulateEvent event){
        if(event.getRightClicked().equals(seat)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = this.getPlayer();
        if (event.getPlayer().equals(player)) {
            stop(false);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().equals(block)) {
            stop(true);
        }

    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.getBlock().equals(block)) {
            stop(true);
        }

    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Player player = this.getPlayer();
        if (event.getEntity().equals(player)) {
            stop(true);
        }

    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = this.getPlayer();
        if (event.getPlayer().equals(player)) {
            event.setTo(event.getPlayer().getLocation());
            stop(true);
        }
    }
}
