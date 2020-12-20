package ru.armagidon.poseplugin.api.utils.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;

public class ScoreboardUtil implements Listener {

    private final Player player;
    private final ScoreboardEventPipelineInjector injector;

    public ScoreboardUtil(Player player) {
        this.player = player;
        this.injector = new ScoreboardEventPipelineInjector();
    }

    public void hideTag(){
        injector.inject(player);
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard.getEntryTeam(player.getName()) == null){
            putToOwnTeam();
        } else {
            mergeTeamSettings(scoreboard.getEntryTeam(player.getName()));
        }
    }

    public void showTag() {
        try {

            Team team = player.getScoreboard().getEntryTeam(player.getName());
            if (team != null) {
                injector.sendAndByPassPacket(this.player, TeamManager.addPlayerToTeam(new TeamWrapper(team.getName()), player));
                NMSUtils.sendPacket(this.player, TeamManager.mergeTeam(new TeamWrapper(team)));
            }

            injector.eject(player);

            //Remove team
            NMSUtils.sendPacket(this.player, TeamManager.removeTeam(new TeamWrapper(player.getName())));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onScoreboardChange(EntryScoreboardChangeEvent event){
        if (event.getPlayer().equals(player)){
            if (event.getMode() == EntryScoreboardChangeEvent.Mode.REMOVE){
                putToOwnTeam();
            } else if (event.getMode() == EntryScoreboardChangeEvent.Mode.ADD){
                Scoreboard scoreboard = player.getScoreboard();
                Team team = scoreboard.getEntryTeam(player.getName());
                if (team != null) {
                    mergeTeamSettings(team);
                } else {
                    try {
                        injector.sendAndByPassPacket(this.player, TeamManager.addPlayerToTeam(new TeamWrapper(player.getName()), player));
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void mergeTeamSettings(Team t) {
        TeamWrapper team = new TeamWrapper(t);

        Team.OptionStatus collisionRule = t.getOption(Team.Option.COLLISION_RULE);

        if (collisionRule.equals(Team.OptionStatus.FOR_OTHER_TEAMS))
            team.setCollisionRule(Team.OptionStatus.NEVER);
        else if(collisionRule.equals(Team.OptionStatus.ALWAYS))
            team.setCollisionRule(Team.OptionStatus.FOR_OWN_TEAM);

        Team.OptionStatus nameTagVisibility = t.getOption(Team.Option.NAME_TAG_VISIBILITY);

        if (nameTagVisibility.equals(Team.OptionStatus.FOR_OTHER_TEAMS))
            team.setCollisionRule(Team.OptionStatus.NEVER);
        else if(nameTagVisibility.equals(Team.OptionStatus.ALWAYS))
            team.setVisibility(Team.OptionStatus.FOR_OWN_TEAM);

        team.setAllowSeeInvisible(false);

        try {
            NMSUtils.sendPacket(this.player, TeamManager.removeTeam(team));
            NMSUtils.sendPacket(this.player, TeamManager.createTeam(team));

            injector.sendAndByPassPacket(this.player, TeamManager.addPlayerToTeam(team, player));

            NMSUtils.sendPacket(this.player, TeamManager.mergeTeam(team));
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void putToOwnTeam(){

        TeamWrapper team = new TeamWrapper(this.player.getName());
        team.setCollisionRule(Team.OptionStatus.FOR_OWN_TEAM);
        team.setVisibility(Team.OptionStatus.FOR_OWN_TEAM);
        team.setAllowSeeInvisible(false);

        try {
            NMSUtils.sendPacket(this.player, TeamManager.removeTeam(team));
            NMSUtils.sendPacket(this.player, TeamManager.createTeam(team));

            injector.sendAndByPassPacket(this.player, TeamManager.addPlayerToTeam(team, player));

            NMSUtils.sendPacket(this.player, TeamManager.mergeTeam(team));
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
