package ru.armagidon.poseplugin.api.utils.scoreboard;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TeamWrapper
{
    //Classes
    private final Class<?> SCOREBOARD = getNmsClass("Scoreboard");
    private final Class<?> SCOREBOARD_TEAM = getNmsClass("ScoreboardTeam");


    private @Getter final String name;
    private @Getter @Setter org.bukkit.scoreboard.Team.OptionStatus visibility;
    private @Getter @Setter org.bukkit.scoreboard.Team.OptionStatus collisionRule;
    private @Getter @Setter boolean allowSeeInvisible;
    private @Getter Object handle;

    public TeamWrapper(String name) {
        this.name = name;
        this.visibility = org.bukkit.scoreboard.Team.OptionStatus.ALWAYS;
        this.collisionRule = org.bukkit.scoreboard.Team.OptionStatus.ALWAYS;
        this.allowSeeInvisible = true;
    }

    public void apply(){
        try {
            //Get team constructor
            Constructor<?> scoreboardTeamConstructor = SCOREBOARD_TEAM.getDeclaredConstructor(SCOREBOARD, String.class);
            //Create an instance of scoreboard team
            Object scoreboardTeam = scoreboardTeamConstructor.newInstance(getScoreboardObject(), getName());
            {
                Method setCanSeeFriendlyInvisibles = getMethodSafely(SCOREBOARD_TEAM, "setCanSeeFriendlyInvisibles", boolean.class);
                setCanSeeFriendlyInvisibles.invoke(scoreboardTeam, this.allowSeeInvisible);
            }
            {
                Class<Enum<?>> ENUM_TEAM_PUSH = getNestedEnum(getNmsClass("ScoreboardTeamBase"),"EnumTeamPush");

                Method setCollisionRule = getMethodSafely(SCOREBOARD_TEAM, "setCollisionRule", ENUM_TEAM_PUSH);

                Enum<?> collisionRule = getEnumValues(ENUM_TEAM_PUSH)[getCollisionRule().ordinal()];

                setCollisionRule.invoke(scoreboardTeam, collisionRule);

            }
            {
                Class<Enum<?>> ENUM_NAMETAG_INVISIBILITY = getNestedEnum(getNmsClass("ScoreboardTeamBase"), "EnumNameTagVisibility");

                Method setNameTagInvisibility = getMethodSafely(SCOREBOARD_TEAM, "setNameTagVisibility", ENUM_NAMETAG_INVISIBILITY);

                Enum<?> visibility = getEnumValues(ENUM_NAMETAG_INVISIBILITY)[getCollisionRule().ordinal()];

                setNameTagInvisibility.invoke(scoreboardTeam, visibility);

            }
            this.handle = scoreboardTeam;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    private Class<?> getNmsClass(String nmsClassName)  {
        return Class.forName("net.minecraft.server." +nmsVersion() + "." + nmsClassName);
    }

    private String nmsVersion(){
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
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

    private <E extends Enum<?>> E[] getEnumValues(Class<E> enumClass) {
        return enumClass.getEnumConstants();
    }

    @SuppressWarnings("unchecked")
    private Class<Enum<?>> getEnum(String name) {
        return (Class<Enum<?>>) getNmsClass(name);
    }

    @SuppressWarnings("unchecked")
    private Class<Enum<?>> getNestedEnum(Class<?> owner, String name) throws ClassNotFoundException {
        return (Class<Enum<?>>) getNestedClass(owner, name);
    }

    private Class<?> getNestedClass(Class<?> owner, String name) throws ClassNotFoundException {
        String path = String.format("%s$%s", owner.getTypeName(), name);
        return Class.forName(path);
    }

}
