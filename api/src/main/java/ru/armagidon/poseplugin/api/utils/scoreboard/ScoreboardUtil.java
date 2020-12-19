package ru.armagidon.poseplugin.api.utils.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

import java.lang.reflect.Method;

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
            Team team = scoreboard.getEntryTeam(player.getName());
            mergeTeamSettings(team.getName(), team.getOption(Team.Option.NAME_TAG_VISIBILITY), team.getOption(Team.Option.COLLISION_RULE));
        }
    }

    public void showTag() {
        injector.eject(player);
        try {
            //Remove team
            NMSUtils.sendPacket(this.player, TeamManager.removeTeam(new TeamWrapper(player.getName())));
            //Resend scoreboard
            Object vanillaPlayer = NMSUtils.asNMSCopy(player);
            Object mcServer = vanillaPlayer.getClass().getDeclaredField("server").get(vanillaPlayer);
            Object playerList = mcServer.getClass().getDeclaredMethod("getPlayerList").invoke(mcServer);

            Method sendScoreboard = ReflectionTools.getNmsClass("PlayerList").getDeclaredMethod("sendScoreboard",
                    ReflectionTools.getNmsClass("ScoreboardServer"), vanillaPlayer.getClass());

            Scoreboard scoreboard = player.getScoreboard();
            Object vanillaScoreboard = scoreboard.getClass().getDeclaredMethod("getHandle").invoke(scoreboard);

            sendScoreboard.invoke(playerList, vanillaScoreboard, vanillaPlayer);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @EventHandler
    public synchronized void onScoreboardChange(EntryScoreboardChangeEvent event){
        if (event.getPlayer().equals(player)){
            if (event.getMode() == EntryScoreboardChangeEvent.Mode.REMOVE){
                putToOwnTeam();
            } else if (event.getMode() == EntryScoreboardChangeEvent.Mode.ADD){
                mergeTeamSettings(event.getTeamName(), event.getNameTagVisibility(), event.getCollisionRule());
            }

        }
    }

    private synchronized void mergeTeamSettings(String teamName, Team.OptionStatus nameTagVisibility, Team.OptionStatus collisionRule) {
        TeamWrapper team = new TeamWrapper(teamName);
        if (collisionRule.equals(Team.OptionStatus.FOR_OTHER_TEAMS))
            team.setCollisionRule(Team.OptionStatus.NEVER);
        else if(collisionRule.equals(Team.OptionStatus.ALWAYS))
            team.setCollisionRule(Team.OptionStatus.FOR_OWN_TEAM);

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

    private synchronized void putToOwnTeam(){

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
