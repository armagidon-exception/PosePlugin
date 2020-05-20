package ru.armagidon.poseplugin.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.armagidon.poseplugin.api.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;

public class StartAnimationEvent extends Event implements Cancellable
{

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled = false;

    private final PosePluginPlayer player;
    private EnumPose pose;

    public StartAnimationEvent(PosePluginPlayer player, EnumPose pose) {
        this.player = player;
        this.pose = pose;
    }

    public PosePluginPlayer getPlayer() {
        return player;
    }

    public EnumPose getPose() {
        return pose;
    }

    public void setPose(EnumPose pose) {
        this.pose = pose;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
