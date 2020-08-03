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


    public NameTagHider() {
        this.teamMap = new HashMap<>();
        if(Bukkit.getScoreboardManager()!=null) {
            scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        }
    }

    public void hideTag(Player player){
        String NAME = player.getName();
        Team team = scoreboard.registerNewTeam(NAME);
        team.setCanSeeFriendlyInvisibles(false);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        team.addEntry(NAME);
        teamMap.put(player, team);
    }

    public void showTag(Player player){
        Team team = teamMap.remove(player);
        if(team!=null) {
            team.removeEntry(player.getName());
            team.unregister();
        }
    }
}
