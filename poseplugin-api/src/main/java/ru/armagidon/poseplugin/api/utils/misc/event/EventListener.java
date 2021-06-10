package ru.armagidon.poseplugin.api.utils.misc.event;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;

//Listener of all necessary events
public class EventListener implements Listener
{
    public EventListener() {}

    @EventHandler
    public void join(PlayerJoinEvent event){
        //Add player to playerlist
        PosePluginAPI.getAPI().getPlayerMap().addPlayer(event.getPlayer());
    }

    @EventHandler
    public void quit(PlayerQuitEvent event){
        //Stop current animation
        if(containsPlayer(event.getPlayer())) {
            PosePluginPlayer ppp = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(event.getPlayer().getName());

            ppp.stopCurrentPose();

            //Remove player from playerlist
            PosePluginAPI.getAPI().getPlayerMap().removePlayer(event.getPlayer());
        }
    }

    private boolean containsPlayer(Player player) {
        return PosePluginAPI.getAPI().getPlayerMap().containsPlayer(player);
    }
}
