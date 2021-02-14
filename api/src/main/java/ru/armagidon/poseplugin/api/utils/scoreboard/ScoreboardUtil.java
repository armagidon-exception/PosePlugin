package ru.armagidon.poseplugin.api.utils.scoreboard;

import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;

import java.util.Collections;

import static ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerScoreboardTeam.Mode.*;

public class ScoreboardUtil implements Listener {

    private final Player player;

    public ScoreboardUtil(Player player) {
        this.player = player;
    }

    public void hideTag() {
        Team playersTeam = player.getScoreboard().getEntryTeam(player.getName());

        if (playersTeam == null) {
            putPlayerToFakeTeam();
        } else {
            mergeTeamSettings(new WrapperScoreboardTeamPacket(playersTeam));
        }
    }

    public void showTag() {
        Team playersTeam = player.getScoreboard().getEntryTeam(player.getName());
        if (playersTeam == null) {
            WrapperScoreboardTeamPacket removePacket = new WrapperScoreboardTeamPacket();
            removePacket.setName(player.getName());
            removePacket.setMode(TEAM_REMOVED);
            removePacket.getHandle().sendPacket(this.player);
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

        if (event.getPacket().getMode() == PLAYERS_ADDED) {
            //merge team settings
            mergeTeamSettings(new WrapperScoreboardTeamPacket(player.getScoreboard().getEntryTeam(player.getName())));
        } else if(event.getPacket().getMode() == PLAYERS_REMOVED){
            //Put to fake team
            putPlayerToFakeTeam();
        } else if (event.getPacket().getMode() == TEAM_UPDATED){
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
            removePacket.getHandle().sendPacket(player);
        }
        {
            //Create new team
            WrapperScoreboardTeamPacket creationPacket = new WrapperScoreboardTeamPacket();
            creationPacket.setMode(TEAM_CREATED);
            creationPacket.setName(player.getName());
            creationPacket.getHandle().sendPacket(player);
        }
        {
            //Add player to fake team
            WrapperScoreboardTeamPacket additionPacket = new WrapperScoreboardTeamPacket();
            additionPacket.setName(player.getName());
            additionPacket.setMode(PLAYERS_ADDED);
            additionPacket.setTeamMateList(Collections.singletonList(player.getName()));
            additionPacket.getHandle().sendPacket(player);
        }
        {
            //Setup team's settings
            WrapperScoreboardTeamPacket updatePacket = new WrapperScoreboardTeamPacket();
            updatePacket.setMode(TEAM_UPDATED);
            updatePacket.setName(player.getName());
            updatePacket.setNameTagVisibility(Team.OptionStatus.FOR_OWN_TEAM);
            updatePacket.setCollisionRule(Team.OptionStatus.FOR_OWN_TEAM);
            updatePacket.setCanSeePlayersInvisibles(false);
            updatePacket.getHandle().sendPacket(player);
        }

    }
}
