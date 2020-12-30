package ru.armagidon.poseplugin.api.utils.scoreboard;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Team;


@Setter
@Getter
public class ScoreboardTeamChangeEvent extends Event
{

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final String teamName;
    private final WrapperScoreboardTeamPacket.Mode mode;
    private final Team.OptionStatus collisionRule;
    private final Team.OptionStatus nameTagVisibility;
    private final boolean marked;
    private final Player player;
    private Object packet;

    public ScoreboardTeamChangeEvent(Player who, Object packet, ScoreboardEventPipelineInjector.PacketData data, boolean marked) {
        super(true);
        this.player = who;
        this.marked = marked;
        this.nameTagVisibility = data.getVisibility();
        this.collisionRule = data.getCollision();
        this.mode = data.getMode();
        this.teamName = data.getTeamName();
        this.packet = packet;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
