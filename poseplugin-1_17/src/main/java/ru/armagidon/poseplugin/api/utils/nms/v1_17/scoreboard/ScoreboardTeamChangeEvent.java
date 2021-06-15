package ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard;

import lombok.Getter;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import static ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard.WrapperScoreboardTeamPacket.*;

@Getter
public class ScoreboardTeamChangeEvent extends Event
{

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private ClientboundSetPlayerTeamPacket packet;

    public ScoreboardTeamChangeEvent(Player player, ClientboundSetPlayerTeamPacket packet) {
        super(true);
        this.player = player;
        this.packet = packet;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public int getMode() {
        ClientboundSetPlayerTeamPacket.Action teamAction = packet.getTeamAction();
        if (teamAction == null) {
            ClientboundSetPlayerTeamPacket.Action playerAction = packet.getPlayerAction();
            if (playerAction == null)
                return TEAM_UPDATED;
            else {
                return switch (playerAction) {
                    case ADD -> PLAYERS_ADDED;
                    case REMOVE -> PLAYERS_REMOVED;
                };
            }
        } else {
            return switch (teamAction) {
                case ADD -> TEAM_CREATED;
                case REMOVE -> TEAM_REMOVED;
            };
        }
    }

    public void setPacket(ClientboundSetPlayerTeamPacket packet) {
        this.packet = packet;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
