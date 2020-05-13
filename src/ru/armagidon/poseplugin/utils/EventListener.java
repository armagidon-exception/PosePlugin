package ru.armagidon.poseplugin.utils;


import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.spigotmc.event.entity.EntityDismountEvent;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.PosePluginPlayer;
import ru.armagidon.poseplugin.poses.EnumPose;
import ru.armagidon.poseplugin.poses.StandingPose;

import java.util.Map;
//Listener of all necessary events
public class EventListener implements org.bukkit.event.Listener
{
    public static Map<String, PosePluginPlayer> players;

    public EventListener(Map<String, PosePluginPlayer> players) {
        EventListener.players = players;
        if(Bukkit.getOnlinePlayers().size()>0){
            Bukkit.getOnlinePlayers().forEach(p-> players.put(p.getName(),new PosePluginPlayer(p.getPlayer())));
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent event){
        players.put(event.getPlayer().getName(),new PosePluginPlayer(event.getPlayer()));
        if(PosePlugin.checker !=null){
            if(!PosePlugin.checker.uptodate&&event.getPlayer().isOp()){
                PosePlugin.checker.sendNotification(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void teleport(PlayerTeleportEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        PosePluginPlayer p  = players.get(event.getPlayer().getName());
        if (!p.getPoseType().equals(EnumPose.STANDING)) {
            if(p.getPoseType().equals(EnumPose.SWIMMING)&&event.getCause().equals(PlayerTeleportEvent.TeleportCause.UNKNOWN)) return;
            Bukkit.getPluginManager().callEvent(new StopAnimationEvent(p.getPoseType(),p, true));
        }
    }

    @EventHandler
    public void death(PlayerDeathEvent event){
        if(!containsPlayer(event.getEntity())) return;
        PosePluginPlayer p = players.get(event.getEntity().getName());
        if (!p.getPoseType().equals(EnumPose.STANDING)) {
            Bukkit.getPluginManager().callEvent(new StopAnimationEvent(p.getPoseType(),p, false));
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent event){
        players.get(event.getPlayer().getName()).getPose().stop(false);
        players.remove(event.getPlayer().getName());
    }

    public boolean containsPlayer(Player player){
        return players.containsKey(player.getName())&&players.get(player.getName())!=null;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        PosePluginPlayer p = players.get(event.getPlayer().getName());
        if (p.getPoseType().equals(EnumPose.LYING)||p.getPoseType().equals(EnumPose.SWIMMING)) {
            Bukkit.getPluginManager().callEvent(new StopAnimationEvent(p.getPoseType(),p, true));
        }
    }

    @EventHandler
    public void onDismount(EntityDismountEvent dismount){
        if(dismount.getEntity() instanceof Player && dismount.getDismounted() instanceof ArmorStand){
            Player player = (Player) dismount.getEntity();
            if(!containsPlayer(player)) return;
            PosePluginPlayer p = players.get(player.getName());
            if(p.getPoseType().equals(EnumPose.SITTING)){
                Bukkit.getPluginManager().callEvent(new StopAnimationEvent(p.getPoseType(),p, true));
            }
        }
    }

    @EventHandler
    public void stop(StopAnimationEvent event){
        event.getPlayer().getPose().stop(event.isLog());
        event.getPlayer().setPose(new StandingPose());
    }
}
