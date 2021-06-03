package ru.armagidon.poseplugin.api.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.IPluginPose;

@AllArgsConstructor
public abstract class PoseEvent extends Event
{
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private @Getter final PosePluginPlayer player;
    protected @Getter IPluginPose newPose;

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
