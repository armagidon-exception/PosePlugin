package ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.AllArgsConstructor;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard.WrapperScoreboardTeamPacket.*;
import static ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard.WrapperScoreboardTeamPacket.TEAM_UPDATED;

@AllArgsConstructor
public class ScoreboardEventPipelineInjector extends ChannelDuplexHandler
{

    private final Player player;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        try {
            //Pass all non-team-related packets
            if (!(msg instanceof ClientboundSetPlayerTeamPacket packet)) {
                super.write(ctx, msg, promise);
                return;
            }

            WrapperScoreboardTeamPacket wrapper = new WrapperScoreboardTeamPacket(packet);
            ScoreboardTeamChangeEvent e = new ScoreboardTeamChangeEvent(player, packet);
            if (!isMarked(packet)) {
                if (wrapper.getMode() == PLAYERS_ADDED || wrapper.getMode() == PLAYERS_REMOVED) {
                    if (wrapper.getPlayers().contains(player.getName())) {
                        Bukkit.getPluginManager().callEvent(e);
                    }
                } else if (wrapper.getMode() == TEAM_UPDATED) {
                    Bukkit.getPluginManager().callEvent(e);
                }
            }

            super.write(ctx, e.getPacket(), promise);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
