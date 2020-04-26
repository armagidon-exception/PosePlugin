package ru.armagidon.sit.utils;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.armagidon.sit.SitPluginPlayer;
import ru.armagidon.sit.poses.EnumPose;

public class StopAnimationEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private final EnumPose pose;
    private final SitPluginPlayer player;


    public StopAnimationEvent(EnumPose pose, SitPluginPlayer player) {
        this.pose = pose;
        this.player = player;
    }

    public EnumPose getPose() {
        return pose;
    }

    public SitPluginPlayer getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }


    public static HandlerList getHandlerList() {
        return handlers;
    }
}
