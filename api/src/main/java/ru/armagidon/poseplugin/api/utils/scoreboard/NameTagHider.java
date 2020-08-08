package ru.armagidon.poseplugin.api.utils.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

public class NameTagHider
{
    private Scoreboard scoreboard;
    private final Map<Player, Team> teamMap;
    private final Map<Player, Team> cached;


    public NameTagHider() {
        this.cached = new HashMap<>();
        this.teamMap = new HashMap<>();
        if(Bukkit.getScoreboardManager()!=null) {
            scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        }
    }

    public void hideTag(Player player){
        String NAME = player.getName();

        if(scoreboard.getEntryTeam(NAME)!=null){
            cached.put(player, scoreboard.getEntryTeam(NAME));
        }

        Team team;
        if(scoreboard.getTeam(NAME)==null) {
            team = scoreboard.registerNewTeam(NAME);
        } else {
            team = scoreboard.getTeam(NAME);
        }
        team.setCanSeeFriendlyInvisibles(false);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        team.addEntry(NAME);
        teamMap.put(player, team);
    }

    public void showTag(Player player){
        if(teamMap.containsKey(player)) {
            Team team = teamMap.remove(player);
            if (team != null) {
                team.removeEntry(player.getName());
                team.unregister();
            }
            if (cached.containsKey(player)) {
                cached.get(player).addEntry(player.getName());
            }
        }
    }
}
