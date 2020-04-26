package ru.armagidon.sit.poses;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import ru.armagidon.sit.SitPlugin;
import ru.armagidon.sit.SitPluginPlayer;

import java.util.Map;

public abstract class PluginPose implements IPluginPose, Listener
{
    private final Player player;

    private final Map<String, SitPluginPlayer> players = ru.armagidon.sit.utils.Listener.players;

    public PluginPose(Player target) {
        this.player = target;
    }

    public Player getPlayer() {
        return player;
    }

    public void play(Player receiver, boolean log){
        if(log) getPlayer().sendMessage(getPose().getMessage());
        Bukkit.getPluginManager().registerEvents(this, SitPlugin.getInstance());
    }

    public void stop(boolean log){
        if(log) getPlayer().sendMessage(EnumPose.STANDING.getMessage());
        HandlerList.unregisterAll(this);
    }

    public abstract EnumPose getPose();

    protected Map<String, SitPluginPlayer> getPlayers(){
        return ru.armagidon.sit.utils.Listener.players;
    }

    public boolean containsPlayer(Player player){
        return players.containsKey(player.getName())&&players.get(player.getName())!=null;
    }
}
