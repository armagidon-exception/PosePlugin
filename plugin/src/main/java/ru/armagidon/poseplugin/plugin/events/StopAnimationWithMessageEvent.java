package ru.armagidon.poseplugin.plugin.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;

@Getter
@AllArgsConstructor
public class StopAnimationWithMessageEvent extends Event
{

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final StopCause cause;
    private final PosePluginPlayer player;
    private final EnumPose pose;

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public enum StopCause {
        DAMAGE,
        OTHER
    }
}
