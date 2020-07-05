package ru.armagidon.poseplugin.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;

public class PoseChangeEvent extends Event implements Cancellable
{

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;
    private final EnumPose before;
    private EnumPose after;
    private final PosePluginPlayer player;

    public PoseChangeEvent(EnumPose before, EnumPose after, PosePluginPlayer player) {
        this.before = before;
        this.after = after;
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public EnumPose getBefore() {
        return before;
    }

    public EnumPose getAfter() {
        return after;
    }

    public void setAfter(EnumPose after) {
        this.after = after;
    }

    public PosePluginPlayer getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
