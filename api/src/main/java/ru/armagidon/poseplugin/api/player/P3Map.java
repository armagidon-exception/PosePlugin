package ru.armagidon.poseplugin.api.player;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class P3Map
{
    private Map<String, PosePluginPlayer> players = new HashMap<>();

    public boolean addPlayer(Player player){
        if(player!=null) {
            players.put(player.getName(), new PosePluginPlayer(player));
            return true;
        } else {
            return false;
        }
    }

    public boolean removePlayer(Player player){
        if(!players.containsKey(player)) return false;
        players.remove(player.getName());
        return true;
    }

    public boolean containsPlayer(Player player){
        return players.containsKey(player.getName())&&players.get(player.getName())!=null;
    }

    public PosePluginPlayer getPosePluginPlayer(String player) {
        return players.get(player);
    }

    public void forEach(Consumer<PosePluginPlayer> action){
        players.values().forEach(action);
    }

}
