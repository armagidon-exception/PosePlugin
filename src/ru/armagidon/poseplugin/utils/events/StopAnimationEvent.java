package ru.armagidon.poseplugin.utils.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.armagidon.poseplugin.PosePluginPlayer;
import ru.armagidon.poseplugin.poses.EnumPose;

public class StopAnimationEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private final EnumPose pose;
    private final PosePluginPlayer player;
    private final boolean log;


    public StopAnimationEvent(EnumPose pose, PosePluginPlayer player, boolean log) {
        this.pose = pose;
        this.player = player;
        this.log = log;
    }

    public EnumPose getPose() {
        return pose;
    }

    public PosePluginPlayer getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }


    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isLog() {
        return log;
    }
}
