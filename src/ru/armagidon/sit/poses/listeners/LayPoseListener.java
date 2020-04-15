package ru.armagidon.sit.poses.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import ru.armagidon.sit.SitPlugin;
import ru.armagidon.sit.SitPluginPlayer;
import ru.armagidon.sit.poses.EnumPose;

import java.util.Map;

import static ru.armagidon.sit.utils.Listener.containsPlayer;

public class LayPoseListener implements Listener
{
    private final Map<String, SitPluginPlayer> players;

    public LayPoseListener(Map<String, SitPluginPlayer> players) {
        SitPlugin.getInstance().getServer().getPluginManager().registerEvents(this,SitPlugin.getInstance());
        this.players = players;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        SitPluginPlayer p = players.get(event.getPlayer().getName());
        if (p.getPose().getPose().equals(EnumPose.LYING) && event.getTo().distance(event.getFrom()) > 1.0D) {
            p.getPose().stop(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Bukkit.getScheduler().runTaskLater(SitPlugin.getInstance(),()->
                players.values().stream().filter(p->p.getPose().getPose().equals(EnumPose.LYING)).forEach(p-> p.getPose().play(event.getPlayer(),false)),5);
    }

    @EventHandler
    public void move(PlayerMoveEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        SitPluginPlayer p =players.get(event.getPlayer().getName());
        if(p.getPose().getPose().equals(EnumPose.LYING)) {
            Location to = event.getTo();
            Location from = event.getFrom();
            if ((to.getX() != from.getX() || (to.getZ() != from.getZ())||(to.getY()!=from.getY()))) {
                event.setCancelled(true);
            }
            p.getPose().move(event);
        }
    }

    @EventHandler
    public void sneak(PlayerToggleSneakEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        SitPluginPlayer p = players.get(event.getPlayer().getName());
        if(p.getPose().getPose().equals(EnumPose.LYING)){
            event.setCancelled(true);
            p.getPose().stop(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        if(!containsPlayer(event.getEntity())) return;
        SitPluginPlayer p = players.get(event.getEntity().getName());
        if(p.getPose().getPose().equals(EnumPose.LYING)) {
            p.getPose().stop(false);
        }
    }

}
