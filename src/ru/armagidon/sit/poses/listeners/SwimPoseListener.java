package ru.armagidon.sit.poses.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import ru.armagidon.sit.SitPlugin;
import ru.armagidon.sit.SitPluginPlayer;
import ru.armagidon.sit.poses.EnumPose;

import java.util.Map;

import static ru.armagidon.sit.utils.Listener.containsPlayer;

public class SwimPoseListener implements Listener
{
    private final Map<String, SitPluginPlayer> players = ru.armagidon.sit.utils.Listener.players;

    public SwimPoseListener() {
        SitPlugin.getInstance().getServer().getPluginManager().registerEvents(this,SitPlugin.getInstance());
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        SitPluginPlayer p = players.get(event.getPlayer().getName());
        if(p.getPose().getPose().equals(EnumPose.SWIM)){
            p.getPose().stop(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Bukkit.getScheduler().runTaskLater(SitPlugin.getInstance(),()->
                players.values().stream().filter(p->p.getPose().getPose().equals(EnumPose.SWIM)).forEach(p-> p.getPose().play(event.getPlayer(),false)),5);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        SitPluginPlayer p = players.get(event.getPlayer().getName());
        if(event.getFrom().getX()!=event.getTo().getX()||event.getFrom().getY()!=event.getTo().getY()||event.getFrom().getZ()!=event.getTo().getZ()) {
            if (p.getPose().getPose().equals(EnumPose.SWIM)) {
                p.getPose().move(event);
            }
        }
    }
}
