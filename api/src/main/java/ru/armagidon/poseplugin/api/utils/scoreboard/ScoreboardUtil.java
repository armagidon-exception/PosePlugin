package ru.armagidon.poseplugin.api.utils.scoreboard;

import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;

import java.util.Collections;

public class ScoreboardUtil implements Listener {

    private final Player player;
    private final ScoreboardEventPipelineInjector injector;

    public ScoreboardUtil(Player player) {
        this.player = player;
        this.injector = new ScoreboardEventPipelineInjector();
    }

    public void hideTag() {
        injector.inject(player);
        Team playersTeam = player.getScoreboard().getEntryTeam(player.getName());

        if (playersTeam == null) {
            putPlayerToFakeTeam();
        } else {
            mergeTeamSettings(playersTeam);
        }
    }

    public void showTag() {
        try {
            injector.eject(player);
            Team playersTeam = player.getScoreboard().getEntryTeam(player.getName());
            if (playersTeam == null) {
                WrapperScoreboardTeamPacket removePacket = new WrapperScoreboardTeamPacket();
                removePacket.setName(player.getName());
                removePacket.setMode(WrapperScoreboardTeamPacket.Mode.REMOVE_TEAM);
                NMSUtils.sendPacket(this.player, removePacket.getHandle());
            } else {
                playersTeam.addEntry(player.getName());

                for (Team.Option option : Team.Option.values()) {
                    playersTeam.setOption(option, playersTeam.getOption(option));
                }

                playersTeam.setCanSeeFriendlyInvisibles(playersTeam.canSeeFriendlyInvisibles());
                playersTeam.setAllowFriendlyFire(playersTeam.allowFriendlyFire());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onScoreboardEvent(ScoreboardTeamChangeEvent event) {
        if (!event.getPlayer().equals(player)) return;

        if (event.getMode().equals(WrapperScoreboardTeamPacket.Mode.ADD_PLAYER)) {
            //merge team settings
            mergeTeamSettings(player.getScoreboard().getEntryTeam(player.getName()));
        } else if(event.getMode().equals(WrapperScoreboardTeamPacket.Mode.REMOVE_PLAYER)){
            //Put to fake team
            putPlayerToFakeTeam();
        } else if (event.getMode().equals(WrapperScoreboardTeamPacket.Mode.UPDATE_TEAM)){
            //fixing team's settings
            event.setPacket(mergeTeamSettings(event.getPacket(), event.getNameTagVisibility(), event.getCollisionRule()));
        }
    }

    private void mergeTeamSettings(Team team) {
        WrapperScoreboardTeamPacket packetWrapper = new WrapperScoreboardTeamPacket(team);

        if (team.getOption(Team.Option.NAME_TAG_VISIBILITY) == Team.OptionStatus.ALWAYS) {
            packetWrapper.setNameTagVisibility(Team.OptionStatus.FOR_OWN_TEAM);
        } else if (team.getOption(Team.Option.NAME_TAG_VISIBILITY) == Team.OptionStatus.FOR_OTHER_TEAMS) {
            packetWrapper.setNameTagVisibility(Team.OptionStatus.NEVER);
        }

        if (team.getOption(Team.Option.COLLISION_RULE) == Team.OptionStatus.ALWAYS) {
            packetWrapper.setCollisionRule(Team.OptionStatus.FOR_OWN_TEAM);
        } else if (team.getOption(Team.Option.COLLISION_RULE) == Team.OptionStatus.FOR_OTHER_TEAMS) {
            packetWrapper.setCollisionRule(Team.OptionStatus.NEVER);
        }

        packetWrapper.setCanSeePlayersInvisibles(false);

        NMSUtils.sendPacket(this.player, packetWrapper.getHandle());
    }

    private Object mergeTeamSettings(Object packet, Team.OptionStatus ntvisibility, Team.OptionStatus collisionRule){
        WrapperScoreboardTeamPacket packetWrapper = new WrapperScoreboardTeamPacket(packet);

        if (ntvisibility == Team.OptionStatus.ALWAYS) {
            packetWrapper.setNameTagVisibility(Team.OptionStatus.FOR_OWN_TEAM);
        } else if (ntvisibility == Team.OptionStatus.FOR_OTHER_TEAMS) {
            packetWrapper.setNameTagVisibility(Team.OptionStatus.NEVER);
        }

        if (collisionRule == Team.OptionStatus.ALWAYS) {
            packetWrapper.setCollisionRule(Team.OptionStatus.FOR_OWN_TEAM);
        } else if (collisionRule == Team.OptionStatus.FOR_OTHER_TEAMS) {
            packetWrapper.setCollisionRule(Team.OptionStatus.NEVER);
        }

        packetWrapper.setCanSeePlayersInvisibles(false);

        return packetWrapper.getHandle();
    }

    @SneakyThrows
    private void putPlayerToFakeTeam() {
        {
            //Remove old team
            WrapperScoreboardTeamPacket removePacket = new WrapperScoreboardTeamPacket();
            removePacket.setName(player.getName());
            removePacket.setMode(WrapperScoreboardTeamPacket.Mode.REMOVE_TEAM);
            NMSUtils.sendPacket(this.player, removePacket.getHandle());
        }
        {
            //Create new team
            WrapperScoreboardTeamPacket creationPacket = new WrapperScoreboardTeamPacket();
            creationPacket.setMode(WrapperScoreboardTeamPacket.Mode.CREATE_TEAM);
            creationPacket.setName(player.getName());
            NMSUtils.sendPacket(this.player, creationPacket.getHandle());
        }
        {
            //Add player to fake team
            WrapperScoreboardTeamPacket additionPacket = new WrapperScoreboardTeamPacket();
            additionPacket.setName(player.getName());
            additionPacket.setMode(WrapperScoreboardTeamPacket.Mode.ADD_PLAYER);
            additionPacket.setTeamMateList(Collections.singletonList(player.getName()));
            NMSUtils.sendPacket(this.player, additionPacket.getHandle());
        }
        {
            //Setup team's settings
            WrapperScoreboardTeamPacket updatePacket = new WrapperScoreboardTeamPacket();
            updatePacket.setMode(WrapperScoreboardTeamPacket.Mode.UPDATE_TEAM);
            updatePacket.setName(player.getName());
            updatePacket.setNameTagVisibility(Team.OptionStatus.FOR_OWN_TEAM);
            updatePacket.setCollisionRule(Team.OptionStatus.FOR_OWN_TEAM);
            updatePacket.setCanSeePlayersInvisibles(false);
            NMSUtils.sendPacket(this.player, updatePacket.getHandle());
        }

    }
}
