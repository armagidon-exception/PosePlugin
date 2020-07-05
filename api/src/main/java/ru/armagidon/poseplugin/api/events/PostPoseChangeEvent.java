package ru.armagidon.poseplugin.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;

public class PostPoseChangeEvent extends Event
{

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final PosePluginPlayer player;
    private final EnumPose pose;

    public PostPoseChangeEvent(PosePluginPlayer player, EnumPose pose) {
        this.player = player;
        this.pose = pose;
    }

    public EnumPose getPose() {
        return pose;
    }

    public PosePluginPlayer getPlayer() {
        return player;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
