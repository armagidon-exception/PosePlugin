package ru.armagidon.poseplugin.utils.misc;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.PosePluginPlayer;
import ru.armagidon.poseplugin.api.events.PoseChangeEvent;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.utils.misc.messaging.Message;

import java.util.Map;
//Listener of all necessary events
public class EventListener implements org.bukkit.event.Listener
{

    private final Map<String, PosePluginPlayer> players;

    public EventListener(Map<String, PosePluginPlayer> players) {
        this.players = players;
        if(Bukkit.getOnlinePlayers().size()>0){
            Bukkit.getOnlinePlayers().forEach(p-> players.put(p.getName(),new PosePluginPlayer(p.getPlayer())));
        }
    }

    @EventHandler
    public void join(PlayerJoinEvent event){
        //Add player to playerlist
        players.put(event.getPlayer().getName(),new PosePluginPlayer(event.getPlayer()));
        //Send notification about new update
        if(PosePlugin.checker !=null){
            if(!PosePlugin.checker.uptodate&&event.getPlayer().hasPermission("poseplugin.admin")){
                PosePlugin.checker.sendNotification(event.getPlayer());
            }
        }
        //Play lay pose animation
        for (PosePluginPlayer pl : players.values()) {
            if(pl.getPoseType().equals(EnumPose.LYING)||pl.getPoseType().equals(EnumPose.SWIMMING)){
                Bukkit.getScheduler().runTaskLater(PosePlugin.getInstance(), ()->
                        pl.getPose().play(event.getPlayer(),false),1L);
            }
        }
        //Inject all packet reader into player's pipeline
        PosePlugin.getInstance().getPacketReaderManager().inject(event.getPlayer());
    }

    @EventHandler
    public void quit(PlayerQuitEvent event){
        //Stop current animation
        players.get(event.getPlayer().getName()).getPose().stop(false);
        //Remove player from playerlist
        players.remove(event.getPlayer().getName());
        //Eject all packet reader out of player's pipeline
        PosePlugin.getInstance().getPacketReaderManager().eject(event.getPlayer());
    }

    private boolean containsPlayer(Player player) {
        return PosePlugin.getInstance().containsPlayer(player);
    }

    @EventHandler
    public void death(PlayerDeathEvent event){
        //If player's not in player list, ignore him
        if(!containsPlayer(event.getEntity())) return;
        PosePluginPlayer p = players.get(event.getEntity().getName());
        //If pose wasn't standing, call stop event
        if (!p.getPoseType().equals(EnumPose.STANDING)) {
            PluginPose.callStopEvent(p.getPoseType(), p, false, StopAnimationEvent.StopCause.DEAHTH);
        }
    }

    @EventHandler
    public void onPoseChange(PoseChangeEvent event){
        if(event.isCancelled()) return;
        if(event.getAfter().equals(EnumPose.LYING)){
            Player player = event.getPlayer().getHandle();
            boolean prevent_invisible = PosePlugin.getInstance().getConfig().getBoolean("lay.prevent-use-when-invisible");
            if(prevent_invisible&&player.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                PosePlugin.getInstance().message().send(Message.LAY_PREVENT_INVISIBILITY, player);
                event.setCancelled(true);
            }
        }
    }
}
