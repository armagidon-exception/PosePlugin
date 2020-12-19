package ru.armagidon.poseplugin.api.utils.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.armagidon.poseplugin.api.PosePluginAPI;

import java.util.HashMap;
import java.util.Map;

public class NameTagHider
{
    private final Scoreboard scoreboard;
    private final Map<Player, Team> teamMap;
    private final Map<Player, Team> cached;
    private final Map<Player, ScoreboardUtil> utils = new HashMap<>();


    public NameTagHider() {
        this.cached = new HashMap<>();
        this.teamMap = new HashMap<>();
        Bukkit.getScoreboardManager();
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public void hideTag(Player player){
        ScoreboardUtil util = new ScoreboardUtil(player);
        PosePluginAPI.getAPI().registerListener(util);
        utils.put(player, util);


        /*String NAME = player.getName();

        Team.OptionStatus option = Team.OptionStatus.FOR_OWN_TEAM;
        if(scoreboard.getEntryTeam(NAME)!=null){
            cached.put(player, scoreboard.getEntryTeam(NAME));
            Team.OptionStatus o = scoreboard.getEntryTeam(NAME).getOption(Team.Option.NAME_TAG_VISIBILITY);
            if(o.equals(Team.OptionStatus.NEVER)||o.equals(Team.OptionStatus.FOR_OTHER_TEAMS)){
                option = o;
            }
        }

        Team team = scoreboard.getTeam(NAME);
        if(team==null) {
            team = scoreboard.registerNewTeam(NAME);
        }
        team.setCanSeeFriendlyInvisibles(false);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, option);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        team.addEntry(NAME);
        teamMap.put(player, team);
        if(player.getScoreboard() != Bukkit.getScoreboardManager().getMainScoreboard()){
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }*/

    }

    public void showTag(Player player){

        if (utils.containsKey(player)){
            ScoreboardUtil util = utils.remove(player);
            HandlerList.unregisterAll(util);
        }

        /*if(teamMap.containsKey(player)) {
            Team team = teamMap.remove(player);
            if (team != null) {
                team.removeEntry(player.getName());
                team.unregister();
            }
            if (cached.containsKey(player)) {
                cached.get(player).addEntry(player.getName());
            }
        }*/
    }
}
