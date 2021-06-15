package ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard;

import io.netty.channel.ChannelPipeline;
import lombok.SneakyThrows;
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
        ((CraftPlayer)player).getHandle().connection.connection.channel.
                pipeline().addBefore("packet_handler", PIPELINE_INJECTOR_NAME, new ScoreboardEventPipelineInjector(player));

        Team playersTeam = player.getScoreboard().getEntryTeam(player.getName());

        if (playersTeam == null) {
            putPlayerToFakeTeam();
        } else {
            WrapperScoreboardTeamPacket packet = mergeTeamSettings(new WrapperScoreboardTeamPacket(playersTeam).setMode(TEAM_UPDATED));
            packet.sendPacket(player);
        }
    }

    public void showTag() {
        ChannelPipeline pipeline = ((CraftPlayer)player).getHandle().connection.connection.channel.pipeline();
        if (pipeline.get(PIPELINE_INJECTOR_NAME) != null)
            pipeline.remove(PIPELINE_INJECTOR_NAME);


        Team playersTeam = player.getScoreboard().getEntryTeam(player.getName());


        if (playersTeam == null) {
            WrapperScoreboardTeamPacket removePlayerPacket= new WrapperScoreboardTeamPacket(player.getName());
            removePlayerPacket.setMode(PLAYERS_REMOVED);
            removePlayerPacket.setTeamMates(Collections.singletonList(player.getName()));
            removePlayerPacket.sendPacket(this.player);

            WrapperScoreboardTeamPacket removePacket = new WrapperScoreboardTeamPacket(player.getName());
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
        //TODO implement team merging

        switch (event.getMode()) {
            case TEAM_UPDATED:
                event.setPacket(mergeTeamSettings(new WrapperScoreboardTeamPacket(event.getPacket())).getHandle());
                break;
            case PLAYERS_ADDED:

                //Remove old team
                WrapperScoreboardTeamPacket removePacket = new WrapperScoreboardTeamPacket(player.getName());
                removePacket.setMode(TEAM_REMOVED);
                removePacket.sendPacket(player);

                Team team = this.player.getScoreboard().getEntryTeam(this.player.getName());
                WrapperScoreboardTeamPacket packet;
                if (team != null) {
                    packet = mergeTeamSettings(new WrapperScoreboardTeamPacket(team).setMode(TEAM_UPDATED));
                } else {
                    packet = mergeTeamSettings(new WrapperScoreboardTeamPacket(event.getPacket()).setMode(TEAM_UPDATED));
                }
                packet.sendPacket(this.player);
                break;
            case PLAYERS_REMOVED:
                putPlayerToFakeTeam();
                break;
            default:
        }
    }

    private WrapperScoreboardTeamPacket mergeTeamSettings(WrapperScoreboardTeamPacket wrapper) {
        if (wrapper.getNameTagVisibility().equals(Team.OptionStatus.ALWAYS)) {
            wrapper.setNameTagVisibility(Team.OptionStatus.FOR_OWN_TEAM);
        } else if (wrapper.getNameTagVisibility().equals(Team.OptionStatus.FOR_OTHER_TEAMS)) {
            wrapper.setNameTagVisibility(Team.OptionStatus.NEVER);
        }

        if (wrapper.getCollisionRule().equals(Team.OptionStatus.ALWAYS)) {
            wrapper.setCollisionRule(Team.OptionStatus.FOR_OWN_TEAM);
        } else if (wrapper.getCollisionRule().equals(Team.OptionStatus.FOR_OTHER_TEAMS)) {
            wrapper.setCollisionRule(Team.OptionStatus.NEVER);
        }

        wrapper.setSeeFriendlyInvisibles(false);
        return wrapper;
    }

    @SneakyThrows
    private void putPlayerToFakeTeam() {
        {
            //Remove old team
            WrapperScoreboardTeamPacket removePacket = new WrapperScoreboardTeamPacket(player.getName());
            removePacket.setMode(TEAM_REMOVED);
            removePacket.sendPacket(player);
        }
        {
            //Create new team
            WrapperScoreboardTeamPacket creationPacket = new WrapperScoreboardTeamPacket(player.getName());
            creationPacket.setMode(TEAM_CREATED);
            creationPacket.sendPacket(player);
        }
        {
            //Add player to fake team
            WrapperScoreboardTeamPacket additionPacket = new WrapperScoreboardTeamPacket(player.getName());
            additionPacket.setMode(PLAYERS_ADDED);
            additionPacket.setTeamMates(Collections.singletonList(player.getName()));
            additionPacket.sendPacket(player);
        }
        {
            //Setup team's settings
            WrapperScoreboardTeamPacket updatePacket = new WrapperScoreboardTeamPacket(player.getName());
            updatePacket.setMode(TEAM_UPDATED);
            updatePacket.setNameTagVisibility(Team.OptionStatus.NEVER);
            updatePacket.setCollisionRule(Team.OptionStatus.NEVER);
            updatePacket.setSeeFriendlyInvisibles(false);
            updatePacket.sendPacket(player);
        }

    }
}
