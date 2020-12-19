package ru.armagidon.poseplugin.api.utils.scoreboard;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Team;


@Getter
public class EntryScoreboardChangeEvent extends Event
{

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final String teamName;
    private final Mode mode;
    private final Player player;
    private final Team.OptionStatus collisionRule;
    private final Team.OptionStatus nameTagVisibility;

    public EntryScoreboardChangeEvent(Player who, String teamName, Mode mode, Team.OptionStatus nameTagVisibility, Team.OptionStatus collisionRule) {
        super(true);
        this.teamName = teamName;
        this.mode = mode;
        this.player = who;
        this.nameTagVisibility = nameTagVisibility;
        this.collisionRule = collisionRule;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public enum Mode {
        ADD, REMOVE
    }
}
