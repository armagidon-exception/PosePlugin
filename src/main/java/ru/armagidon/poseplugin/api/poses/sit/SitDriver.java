package ru.armagidon.poseplugin.api.poses.sit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.spigotmc.event.entity.EntityDismountEvent;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.utils.misc.ticking.Tickable;

import java.lang.reflect.Field;

public class SitDriver implements Listener, Tickable
{
    private final Runnable execute;
    private ArmorStand seat;
    private final Player sitter;

    public SitDriver(Player sitter, Runnable onDismount) {
        Bukkit.getPluginManager().registerEvents(this, PosePlugin.getInstance());
        this.sitter = sitter;
        this.execute = onDismount;
    }

    public void takeASeat() {
        Location location = sitter.getLocation().clone();
        seat = sitter.getWorld().spawn(location.clone().subtract(0, 0.2D, 0), ArmorStand.class, (armorStand -> {
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setVisible(false);
            armorStand.setCollidable(false);
            armorStand.addPassenger(sitter);
        }));
    }

    public void standUp() {
        HandlerList.unregisterAll(this);
        seat.remove();
        if(!PosePlugin.getInstance().getStatus().equals(PosePlugin.ServerStatus.SHUTTING_DOWN)) {
            Bukkit.getScheduler().runTaskLater(PosePlugin.getInstance(), () ->
                    sitter.teleport(seat.getLocation().clone().add(0, 0.3D, 0).setDirection(sitter.getLocation().getDirection())), 1);
        }
    }

    @EventHandler
    private void armorManipulate(PlayerArmorStandManipulateEvent event){
        if(event.getRightClicked().equals(seat)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void playerStoodUpEvent(EntityDismountEvent event){
        if(event.getEntity().getType().equals(EntityType.PLAYER)&&event.getDismounted().getType().equals(EntityType.ARMOR_STAND)){
            ArmorStand stand = (ArmorStand) event.getDismounted();
            Player player = (Player) event.getEntity();
            if(player.getUniqueId().equals(sitter.getUniqueId())&&stand.equals(seat)){
                //If player dismounted from seat, do stuff
                execute.run();
            }
        }
    }

    public void tick(){
        try {
            Object vanillaStand = seat.getClass().getMethod("getHandle").invoke(seat);
            Field yawF = vanillaStand.getClass().getField("yaw");
            yawF.set(vanillaStand, sitter.getLocation().getYaw());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
