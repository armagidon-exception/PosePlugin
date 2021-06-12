package ru.armagidon.poseplugin.api.utils.nms.protocolized.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerScoreboardTeam;

import java.util.Map;

import static ru.armagidon.poseplugin.api.utils.nms.protocolized.scoreboard.WrapperScoreboardTeamPacket.isMarked;

public class ScoreboardEventPipelineInjector extends PacketAdapter
{

    private final Map<Player, ScoreboardUtil> hiddenPlayers;

    public ScoreboardEventPipelineInjector(Plugin plugin, Map<Player, ScoreboardUtil> hiddenPlayers) {
        super(plugin, PacketType.Play.Server.SCOREBOARD_TEAM);
        this.hiddenPlayers = hiddenPlayers;
    }


    @Override
    public void onPacketSending(PacketEvent event) {
        if (!hiddenPlayers.containsKey(event.getPlayer())) return;
        WrapperPlayServerScoreboardTeam packet = new WrapperPlayServerScoreboardTeam(event.getPacket());
        ScoreboardTeamChangeEvent e = new ScoreboardTeamChangeEvent(event.getPlayer(), packet);
        if (!isMarked(packet)) {
            if (packet.getMode() == WrapperPlayServerScoreboardTeam.Mode.PLAYERS_ADDED || packet.getMode() == WrapperPlayServerScoreboardTeam.Mode.PLAYERS_REMOVED) {
                if (packet.getPlayers().contains(event.getPlayer().getName())) {
                    Bukkit.getPluginManager().callEvent(e);
                }
            } else if (packet.getMode() == WrapperPlayServerScoreboardTeam.Mode.TEAM_UPDATED) {
                Bukkit.getPluginManager().callEvent(e);
            }
            event.setPacket(e.getPacket().getHandle());
        }
    }
}
