package ru.armagidon.sit.poses;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.armagidon.sit.SitPluginPlayer;

import java.util.Map;

public abstract class PluginPose
{
    private final Player player;

    public PluginPose(Player target) {
        this.player = target;
    }

    public Player getPlayer() {
        return player;
    }

    public abstract void play(Player receiver, boolean log);

    public abstract void stop(boolean log);

    public abstract EnumPose getPose();

    protected Map<String, SitPluginPlayer> getPlayers(){
        return ru.armagidon.sit.utils.Listener.players;
    }

    public void move(PlayerMoveEvent event){}
    public void armor(PlayerArmorStandManipulateEvent event){}
}
