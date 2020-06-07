package ru.armagidon.poseplugin.utils.misc;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginPlayer;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.utils.nms.NMSUtils;

import java.util.Map;

//Listener of all necessary events
public class EventListener implements org.bukkit.event.Listener {
    private final Map<String, PosePluginPlayer> players;

    public EventListener(Map<String, PosePluginPlayer> players) {
        this.players = players;
        if (Bukkit.getOnlinePlayers().size() > 0) {
            Bukkit.getOnlinePlayers().forEach(p -> players.put(p.getName(), new PosePluginPlayer(p.getPlayer())));
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        //Add player to playerlist
        players.put(event.getPlayer().getName(), new PosePluginPlayer(event.getPlayer()));
        //Send notification about new update
        if (PosePlugin.checker != null) {
            if (!PosePlugin.checker.uptodate && event.getPlayer().isOp()) {
                PosePlugin.checker.sendNotification(event.getPlayer());
            }
        }
        //Play lay pose animation
        for (PosePluginPlayer pl : players.values()) {
            if (pl.getPoseType().equals(EnumPose.LYING) || pl.getPoseType().equals(EnumPose.SWIMMING)) {
                Bukkit.getScheduler().runTaskLater(PosePlugin.getInstance(), () -> {
                    pl.getPose().play(event.getPlayer(), false);
                }, 1L);
            }
        }
        //Inject damage packet listener to player's channel pipeline
        NMSUtils.getSwingReader(event.getPlayer()).inject();
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        //Stop current animation
        players.get(event.getPlayer().getName()).getPose().stop(false);
        //Remove player from playerlist
        players.remove(event.getPlayer().getName());
        //Eject damage packet reader out of player's channel pipeline
        NMSUtils.getSwingReader(event.getPlayer()).eject();
    }

    @EventHandler
    public void teleport(PlayerTeleportEvent event) {
        //If player's not in player list, ignore him
        if (!containsPlayer(event.getPlayer())) return;
        PosePluginPlayer p = players.get(event.getPlayer().getName());
        //If animation isn't standing, stop animation
        if (!p.getPoseType().equals(EnumPose.STANDING)) {
            //if animation was swimming and teleport cause was unknown, ignore it.
            if (p.getPoseType().equals(EnumPose.SWIMMING) && event.getCause().equals(PlayerTeleportEvent.TeleportCause.UNKNOWN))
                return;
            if (p.getPoseType().equals(EnumPose.LYING) && event.getCause().equals(PlayerTeleportEvent.TeleportCause.UNKNOWN))
                return;
            //Call StopAnimationEvent
            PluginPose.callStopEvent(p.getPoseType(), p, true, StopAnimationEvent.StopCause.TELEPORT);
        }
    }

    private boolean containsPlayer(Player player) {
        return PosePlugin.getInstance().containsPlayer(player);
    }

    @EventHandler
    public void death(PlayerDeathEvent event) {
        //If player's not in player list, ignore him
        if (!containsPlayer(event.getEntity())) return;
        PosePluginPlayer p = players.get(event.getEntity().getName());
        //If pose wasn't standing, call stop event
        if (!p.getPoseType().equals(EnumPose.STANDING))
            PluginPose.callStopEvent(p.getPoseType(), p, false, StopAnimationEvent.StopCause.STOPPED);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        //If player's not in player list, ignore him
        if (!containsPlayer(event.getPlayer())) return;
        PosePluginPlayer p = players.get(event.getPlayer().getName());
        //Call stop event
        if (p.getPoseType().equals(EnumPose.SWIMMING))
            PluginPose.callStopEvent(p.getPoseType(), p, true, StopAnimationEvent.StopCause.STOPPED);
    }
}
