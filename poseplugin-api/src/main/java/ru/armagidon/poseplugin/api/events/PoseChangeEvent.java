package ru.armagidon.poseplugin.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.IPluginPose;

public class PoseChangeEvent extends PoseEvent implements Cancellable
{

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;
    private final EnumPose before;

    public PoseChangeEvent(EnumPose before, IPluginPose after, PosePluginPlayer player) {
        super(player, after);
        this.before = before;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public EnumPose getBefore() {
        return before;
    }

    public void setNewPose(IPluginPose newPose) {
        this.newPose = newPose;
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
