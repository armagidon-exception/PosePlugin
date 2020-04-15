package ru.armagidon.sit.utils;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.armagidon.sit.SitPluginPlayer;
import ru.armagidon.sit.poses.listeners.LayPoseListener;
import ru.armagidon.sit.poses.listeners.SitPoseListener;
import ru.armagidon.sit.poses.listeners.SwimPoseListener;

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
        new LayPoseListener(players);
        new SitPoseListener();
        new SwimPoseListener();
    }

    @EventHandler
    public void join(PlayerJoinEvent event){
        players.put(event.getPlayer().getName(),new SitPluginPlayer(event.getPlayer()));
    }

    @EventHandler
    public void quit(PlayerQuitEvent event){
        players.remove(event.getPlayer().getName());
    }


    public static boolean containsPlayer(Player player){
        return players.containsKey(player.getName())&&players.get(player.getName())!=null;
    }
}
