package ru.armagidon.poseplugin.api.events;

import lombok.Getter;
import lombok.Setter;
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
    private @Getter @Setter boolean cancelled;
    private @Getter final StopCause cause;
    private @Getter final String customCause;

    public StopAnimationEvent(EnumPose pose, PosePluginPlayer player, StopCause cause, String custom_cause) {
        this.pose = pose;
        this.player = player;
        this.cause = cause;
        this.customCause = custom_cause;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @SuppressWarnings("DanglingJavadoc")
    public enum StopCause{
        STOPPED, /**Called when animation has been stopped by user*/

        QUIT /**Called when animation has been stopped because of player's quit(you can't cancel it)*/
    }
}
