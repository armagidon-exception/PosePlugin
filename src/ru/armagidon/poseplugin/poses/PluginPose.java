package ru.armagidon.poseplugin.poses;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.PosePluginPlayer;
import ru.armagidon.poseplugin.utils.EventListener;

import java.util.Map;

public abstract class PluginPose implements IPluginPose, Listener
{
    private final Player player;

    private final Map<String, PosePluginPlayer> players = EventListener.players;

    public PluginPose(Player target) {
        this.player = target;
    }

    public Player getPlayer() {
        return player;
    }

    public void play(Player receiver, boolean log){
        if(log) getPlayer().sendMessage(getPose().getMessage());
        Bukkit.getPluginManager().registerEvents(this, PosePlugin.getInstance());
    }

    public void stop(boolean log){
        if(log) getPlayer().sendMessage(EnumPose.STANDING.getMessage());
        HandlerList.unregisterAll(this);
    }

    public abstract EnumPose getPose();

    protected Map<String, PosePluginPlayer> getPlayers(){
        return EventListener.players;
    }

    public boolean containsPlayer(Player player){
        return players.containsKey(player.getName())&&players.get(player.getName())!=null;
    }
}
