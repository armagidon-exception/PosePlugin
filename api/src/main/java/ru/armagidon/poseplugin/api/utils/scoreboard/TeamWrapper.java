package ru.armagidon.poseplugin.api.utils.scoreboard;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TeamWrapper
{
    //Classes
    private final Class<?> SCOREBOARD = ReflectionTools.getNmsClass("Scoreboard");
    private final Class<?> SCOREBOARD_TEAM = ReflectionTools.getNmsClass("ScoreboardTeam");


    private @Getter final String name;
    private @Getter @Setter Team.OptionStatus visibility;
    private @Getter @Setter Team.OptionStatus collisionRule;
    private @Getter @Setter boolean allowSeeInvisible;
    private @Getter final Object scoreboardTeam;

    public TeamWrapper(String name) {
        this.name = name;
        this.visibility = org.bukkit.scoreboard.Team.OptionStatus.ALWAYS;
        this.collisionRule = org.bukkit.scoreboard.Team.OptionStatus.ALWAYS;
        this.allowSeeInvisible = true;
        this.scoreboardTeam = createTeamInstance(name);
    }

    public TeamWrapper(Team team) {
        this.scoreboardTeam = convertToTeamWrapper(team);
        this.name = team.getName();
        this.visibility = team.getOption(Team.Option.NAME_TAG_VISIBILITY);
        this.collisionRule = team.getOption(Team.Option.COLLISION_RULE);
        this.allowSeeInvisible = team.canSeeFriendlyInvisibles();
    }

    public void apply(){
        try {
            {
                Method setCanSeeFriendlyInvisibles = getMethodSafely(SCOREBOARD_TEAM, "setCanSeeFriendlyInvisibles", boolean.class);
                setCanSeeFriendlyInvisibles.invoke(scoreboardTeam, this.allowSeeInvisible);
            }
            {
                Class<Enum<?>> ENUM_TEAM_PUSH = ReflectionTools.getNestedEnum(ReflectionTools.getNmsClass("ScoreboardTeamBase"),"EnumTeamPush");

                Method setCollisionRule = getMethodSafely(SCOREBOARD_TEAM, "setCollisionRule", ENUM_TEAM_PUSH);

                Enum<?> collisionRule = ReflectionTools.getEnumValues(ENUM_TEAM_PUSH)[getCollisionRule().ordinal()];

                setCollisionRule.invoke(scoreboardTeam, collisionRule);

            }
            {
                Class<Enum<?>> ENUM_NAMETAG_INVISIBILITY = ReflectionTools.getNestedEnum(ReflectionTools.getNmsClass("ScoreboardTeamBase"), "EnumNameTagVisibility");

                Method setNameTagInvisibility = getMethodSafely(SCOREBOARD_TEAM, "setNameTagVisibility", ENUM_NAMETAG_INVISIBILITY);

                Enum<?> visibility = ReflectionTools.getEnumValues(ENUM_NAMETAG_INVISIBILITY)[getCollisionRule().ordinal()];

                setNameTagInvisibility.invoke(scoreboardTeam, visibility);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    @SneakyThrows
    private Object createTeamInstance(String name) {
        //Get team constructor
        Constructor<?> scoreboardTeamConstructor = SCOREBOARD_TEAM.getDeclaredConstructor(SCOREBOARD, String.class);
        //Create an instance of scoreboard team
        return scoreboardTeamConstructor.newInstance(getScoreboardObject(), name);
    }

    @SneakyThrows
    private Object convertToTeamWrapper(Team team){
        Field teamF =  team.getClass().getDeclaredField("team");
        teamF.setAccessible(true);
        Object vanilla = teamF.get(team);

        Object newTeam = createTeamInstance(team.getName());

        for (Field field : vanilla.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            field.set(newTeam, field.get(vanilla));
        }

        return newTeam;

    }

    @SneakyThrows
    private Method getMethodSafely(Class<?> clazz, String name, Class<?>... argTypes){
        return clazz.getDeclaredMethod(name, argTypes);
    }

    @SneakyThrows
    public Object getScoreboardObject(){
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        return scoreboard.getClass().getDeclaredMethod("getHandle").invoke(scoreboard);
    }

}
