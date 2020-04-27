package ru.armagidon.sit.utils;


import de.Kurfat.Java.Minecraft.BetterChair.PlayerSitEvent;
import de.Kurfat.Java.Minecraft.BetterChair.Types.Chair;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;
import ru.armagidon.sit.SitPlugin;
import ru.armagidon.sit.SitPluginPlayer;
import ru.armagidon.sit.poses.EnumPose;
import ru.armagidon.sit.poses.StandingPose;
import ru.armagidon.sit.poses.sit.ExternalSitPose;

import java.util.Map;

//Listener of all necessary events
public class Listener implements org.bukkit.event.Listener
{
    public static Map<String, SitPluginPlayer> players;

    public Listener(Map<String, SitPluginPlayer> players) {
        Listener.players = players;
        if(Bukkit.getOnlinePlayers().size()>0){
            Bukkit.getOnlinePlayers().forEach(p-> players.put(p.getName(),new SitPluginPlayer(p.getPlayer())));
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent event){
        players.put(event.getPlayer().getName(),new SitPluginPlayer(event.getPlayer()));
        if(Utils.CHECK_FOR_UPDATED)new UpdateChecker().runTaskAsynchronously(SitPlugin.getInstance());
    }

    @EventHandler
    public void quit(PlayerQuitEvent event){
        players.remove(event.getPlayer().getName());
    }

    public boolean containsPlayer(Player player){
        return players.containsKey(player.getName())&&players.get(player.getName())!=null;
    }

    @EventHandler
    public void onShift(PlayerToggleSneakEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        SitPluginPlayer p = players.get(event.getPlayer().getName());
        if (p.getPoseType().equals(EnumPose.LYING)||p.getPoseType().equals(EnumPose.SWIM)) {
            Bukkit.getPluginManager().callEvent(new StopAnimationEvent(p.getPoseType(),p));
        }
    }

    @EventHandler
    public void onDismount(EntityDismountEvent dismount){
        if(dismount.getEntity() instanceof Player && dismount.getDismounted() instanceof ArmorStand){
            Player player = (Player) dismount.getEntity();
            if(!containsPlayer(player)) return;
            SitPluginPlayer p = players.get(player.getName());
            if(p.getPoseType().equals(EnumPose.SITTING)){
                Bukkit.getPluginManager().callEvent(new StopAnimationEvent(p.getPoseType(),p));
            }
        }
    }

    @EventHandler
    public void stop(StopAnimationEvent event){
        event.getPlayer().getPose().stop(true);
        event.getPlayer().setPose(new StandingPose());
    }

    @EventHandler
    public void onSit(EntityMountEvent event){
        if(SitPlugin.bridge.isActivated()) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (Chair.CACHE_BY_PLAYER.containsKey(player)) {
                    Bukkit.getPluginManager().callEvent(new PlayerSitEvent(player, Chair.CACHE_BY_PLAYER.get(player), true));
                }
            }
        }
    }

    @EventHandler
    public void sit(PlayerSitEvent event){
        if(SitPlugin.bridge.isActivated()) {
            if(event.isEnable()) {
                SitPluginPlayer p = players.get(event.getPlayer().getName());
                p.setPose(new ExternalSitPose(event.getPlayer()));
                p.getPlayer().sendMessage(EnumPose.SITTING.getMessage());
            }
        }
    }
}