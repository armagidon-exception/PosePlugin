package ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import lombok.SneakyThrows;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;

import java.util.Collections;

import static ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard.WrapperScoreboardTeamPacket.*;

public class ScoreboardUtil implements Listener {

    private final Player player;

    private static final String PIPELINE_INJECTOR_NAME = "poseplugin_pipeline_inject";

    public ScoreboardUtil(Player player) {
        this.player = player;
    }

    public void hideTag() {
        ((CraftPlayer)player).getHandle().connection.connection.channel.pipeline().addBefore("packet_handler", PIPELINE_INJECTOR_NAME, new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                //Pass all non-team-related packets
                if (!(msg instanceof ClientboundSetPlayerTeamPacket packet)) {
                    super.channelRead(ctx, msg);
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
                    super.channelRead(ctx, packet);
                }
            }
        });
        Team playersTeam = player.getScoreboard().getEntryTeam(player.getName());

        if (playersTeam == null) {
            putPlayerToFakeTeam();
        } else {
            mergeTeamSettings(new WrapperScoreboardTeamPacket(playersTeam));
        }
    }

    public void showTag() {
        ChannelPipeline pipeline =((CraftPlayer)player).getHandle().connection.connection.channel.pipeline();
        if (pipeline.get(PIPELINE_INJECTOR_NAME) != null)
            pipeline.remove(PIPELINE_INJECTOR_NAME);
        Team playersTeam = player.getScoreboard().getEntryTeam(player.getName());
        if (playersTeam == null) {
            WrapperScoreboardTeamPacket removePacket = new WrapperScoreboardTeamPacket();
            removePacket.setName(player.getName());
            removePacket.setMode(TEAM_REMOVED);
            removePacket.sendPacket(this.player);
        } else {
            playersTeam.addEntry(player.getName());

            for (Team.Option option : Team.Option.values()) {
                playersTeam.setOption(option, playersTeam.getOption(option));
            }

            playersTeam.setCanSeeFriendlyInvisibles(playersTeam.canSeeFriendlyInvisibles());
            playersTeam.setAllowFriendlyFire(playersTeam.allowFriendlyFire());
        }
    }

    @EventHandler
    public void onScoreboardEvent(ScoreboardTeamChangeEvent event) {
        if (!event.getPlayer().equals(player)) return;

        if (event.getMode() == PLAYERS_ADDED) {
            //merge team settings
            mergeTeamSettings(new WrapperScoreboardTeamPacket(player.getScoreboard().getEntryTeam(player.getName())));
        } else if(event.getMode() == PLAYERS_REMOVED){
            //Put to fake team
            putPlayerToFakeTeam();
        } else if (event.getMode() == TEAM_UPDATED){
            //fixing team's settings
            mergeTeamSettings(new WrapperScoreboardTeamPacket(event.getPacket()));
        }
    }

    private void mergeTeamSettings(WrapperScoreboardTeamPacket packetWrapper){

        if (packetWrapper.getNameTagVisibility().equals(Team.OptionStatus.ALWAYS)) {
            packetWrapper.setNameTagVisibility(Team.OptionStatus.FOR_OWN_TEAM);
        } else if (packetWrapper.getNameTagVisibility().equals(Team.OptionStatus.FOR_OTHER_TEAMS)) {
            packetWrapper.setNameTagVisibility(Team.OptionStatus.NEVER);
        }

        if (packetWrapper.getCollisionRule().equals(Team.OptionStatus.ALWAYS)) {
            packetWrapper.setCollisionRule(Team.OptionStatus.FOR_OWN_TEAM);
        } else if (packetWrapper.getCollisionRule().equals(Team.OptionStatus.FOR_OTHER_TEAMS)) {
            packetWrapper.setCollisionRule(Team.OptionStatus.NEVER);
        }

        packetWrapper.setCanSeePlayersInvisibles(false);
    }

    @SneakyThrows
    private void putPlayerToFakeTeam() {
        {
            //Remove old team
            WrapperScoreboardTeamPacket removePacket = new WrapperScoreboardTeamPacket();
            removePacket.setName(player.getName());
            removePacket.setMode(TEAM_REMOVED);
            removePacket.sendPacket(player);
        }
        {
            //Create new team
            WrapperScoreboardTeamPacket creationPacket = new WrapperScoreboardTeamPacket();
            creationPacket.setMode(TEAM_CREATED);
            creationPacket.setName(player.getName());
            creationPacket.sendPacket(player);
        }
        {
            //Add player to fake team
            WrapperScoreboardTeamPacket additionPacket = new WrapperScoreboardTeamPacket();
            additionPacket.setName(player.getName());
            additionPacket.setMode(PLAYERS_ADDED);
            additionPacket.setTeamMateList(Collections.singletonList(player.getName()));
            additionPacket.sendPacket(player);
        }
        {
            //Setup team's settings
            WrapperScoreboardTeamPacket updatePacket = new WrapperScoreboardTeamPacket();
            updatePacket.setMode(TEAM_UPDATED);
            updatePacket.setName(player.getName());
            updatePacket.setNameTagVisibility(Team.OptionStatus.FOR_OWN_TEAM);
            updatePacket.setCollisionRule(Team.OptionStatus.FOR_OWN_TEAM);
            updatePacket.setCanSeePlayersInvisibles(false);
            updatePacket.sendPacket(player);
        }

    }
}
