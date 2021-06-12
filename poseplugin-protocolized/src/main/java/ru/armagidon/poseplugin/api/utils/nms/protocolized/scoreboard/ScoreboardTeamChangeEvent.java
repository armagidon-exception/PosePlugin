package ru.armagidon.poseplugin.api.utils.nms.protocolized.scoreboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerScoreboardTeam;

@Getter
@AllArgsConstructor
public class ScoreboardTeamChangeEvent extends Event
{

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final WrapperPlayServerScoreboardTeam packet;

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
