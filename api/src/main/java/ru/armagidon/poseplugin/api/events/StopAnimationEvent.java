package ru.armagidon.poseplugin.api.events;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;

public class StopAnimationEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    private @Getter final EnumPose pose;
    private @Getter final PosePluginPlayer player;
    private @Getter boolean cancelled;
    private @Getter final boolean cancellable;

    public StopAnimationEvent(EnumPose pose, PosePluginPlayer player, boolean cancellable) {
        this.pose = pose;
        this.player = player;
        this.cancellable = cancellable;
    }

    public void setCancelled(boolean cancelled) {
        if( !cancellable ) return;
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
