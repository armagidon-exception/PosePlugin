package ru.armagidon.poseplugin.api.utils.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;

public class TeamManager
{

    private static final String PACKET_NAME = "PacketPlayOutScoreboardTeam";
    private static final String TEAM_CLASS = "ScoreboardTeam";


    public static Object createTeam(TeamWrapper team) throws Exception {

        team.apply();

        Object scoreboardTeam = team.getScoreboardTeam();

        Constructor<?> creationConstr = getNmsClass(PACKET_NAME).getDeclaredConstructor(getNmsClass(TEAM_CLASS), int.class);

        return creationConstr.newInstance(scoreboardTeam, 0);
    }


    public static Object removeTeam(TeamWrapper team) throws Exception {

        team.apply();

        Constructor<?> creationConstr = getNmsClass(PACKET_NAME).getDeclaredConstructor(getNmsClass(TEAM_CLASS), int.class);

        return creationConstr.newInstance(team.getScoreboardTeam(), 1);
    }

    public static Object mergeTeam(TeamWrapper team) throws Exception {
        team.apply();

        Constructor<?> creationConstr = getNmsClass(PACKET_NAME).getDeclaredConstructor(getNmsClass(TEAM_CLASS), int.class);

        return creationConstr.newInstance(team.getScoreboardTeam(), 2);
    }

    public static Object addPlayerToTeam(TeamWrapper team, Player player) throws Exception {
        team.apply();

        Constructor<?> additionConstr = getNmsClass(PACKET_NAME).getDeclaredConstructor(getNmsClass(TEAM_CLASS), Collection.class, int.class);

        return additionConstr.newInstance(team.getScoreboardTeam(), Collections.singletonList(player.getName()), 3);
    }


    public static Object removePlayerFromTeam(TeamWrapper team, Player player) throws Exception {
        team.apply();
        Constructor<?> additionConstr = getNmsClass(PACKET_NAME).getDeclaredConstructor(getNmsClass(TEAM_CLASS), Collection.class, int.class);

        return additionConstr.newInstance(team.getScoreboardTeam(), Collections.singletonList(player.getName()), 4);
    }


    private static Class<?> getNmsClass(String nmsClassName) throws Exception {
        return Class.forName("net.minecraft.server." +nmsVersion() + "." + nmsClassName);
    }

    public static String nmsVersion(){
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

}
