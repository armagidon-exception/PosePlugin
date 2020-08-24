package ru.armagidon.poseplugin.api.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.utils.nms.npc.HandType;

public class HandTypeChangeEvent extends Event implements Cancellable
{
    private final static HandlerList HANDLER_LIST = new HandlerList();
    private final @Getter HandType oldMode;
    private final @Getter EnumPose pose;
    private final @Getter PosePluginPlayer player;
    @Getter @Setter private boolean cancelled;
    @Getter @Setter private HandType newMode;

    public HandTypeChangeEvent(HandType oldMode, HandType newMode, EnumPose pose, PosePluginPlayer player){
        this.oldMode = oldMode;
        this.newMode = newMode;
        this.pose = pose;
        this.player = player;
    }

    public boolean call() {
        Bukkit.getPluginManager().callEvent(this);
        return !((Cancellable)this).isCancelled();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
