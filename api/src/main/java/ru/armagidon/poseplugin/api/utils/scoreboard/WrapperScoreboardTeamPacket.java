package ru.armagidon.poseplugin.api.utils.scoreboard;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.bukkit.scoreboard.Team;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;

import static ru.armagidon.poseplugin.api.utils.nms.ReflectionTools.*;

public class WrapperScoreboardTeamPacket
{
    private static final String PACKET_NAME = "PacketPlayOutScoreboardTeam";

    private static final ImmutableMap<Team.OptionStatus, String> pushBukkitToNotch = ImmutableMap.<Team.OptionStatus, String>builder().
            put(Team.OptionStatus.ALWAYS, "always").put(Team.OptionStatus.NEVER, "never").put(Team.OptionStatus.FOR_OTHER_TEAMS, "pushOtherTeams").
            put(Team.OptionStatus.FOR_OWN_TEAM, "pushOwnTeam").build();

    private static final ImmutableMap<Team.OptionStatus, String> hideBukkitToNotch = ImmutableMap.<Team.OptionStatus, String>builder().
            put(Team.OptionStatus.ALWAYS, "always").put(Team.OptionStatus.NEVER, "never").put(Team.OptionStatus.FOR_OTHER_TEAMS, "hideForOtherTeams").
            put(Team.OptionStatus.FOR_OWN_TEAM, "hideForOwnTeam").build();

    private static final byte marker = 66;

    private final Object handle;

    public WrapperScoreboardTeamPacket(Team team) {
        this.handle = createPacket(team);
        markPacket();
    }

    public WrapperScoreboardTeamPacket(Object handle) {
        this.handle = handle;
        markPacket();
    }

    public WrapperScoreboardTeamPacket() {
        handle = createEmptyPacket();
        markPacket();
    }

    public void setNameTagVisibility(Team.OptionStatus nameTagVisibility) {
        try {
            Field nameTagVisibilityF = handle.getClass().getDeclaredField("e");
            nameTagVisibilityF.setAccessible(true);

            nameTagVisibilityF.set(handle, hideBukkitToNotch.get(nameTagVisibility));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setCollisionRule(Team.OptionStatus collisionRule) {
        try {
            Field collisionRuleF = handle.getClass().getDeclaredField("f");
            collisionRuleF.setAccessible(true);

            collisionRuleF.set(handle, pushBukkitToNotch.get(collisionRule));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setCanSeePlayersInvisibles(boolean flag){
        try {
            Field optionsF = handle.getClass().getDeclaredField("j");
            optionsF.setAccessible(true);
            int curr = optionsF.getInt(handle);
            optionsF.set(handle, (int) setBit((byte) curr, 1, flag));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setName(String name) {
        try {
            Field nameF = handle.getClass().getDeclaredField("a");
            nameF.setAccessible(true);
            nameF.set(handle, name);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setMode(Mode mode) {
        try {
            Field nameF = handle.getClass().getDeclaredField("i");
            nameF.setAccessible(true);
            nameF.set(handle, mode.ordinal());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setTeamMateList(Collection<String> players){
        try {
            Field nameF = handle.getClass().getDeclaredField("h");
            nameF.setAccessible(true);
            nameF.set(handle, players);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static byte setBit(byte input, int k, boolean flag){
        byte output;
        if(flag){
            output = (byte) (input | (1 << k));
        } else {
            output = (byte) (input & ~(1 << k));
        }
        return output;
    }

    public Object getHandle() {
        return handle;
    }

    @SneakyThrows
    private Object createPacket(Team team) {
        Field teamF =  team.getClass().getDeclaredField("team");
        teamF.setAccessible(true);
        Object vanilla = teamF.get(team);

        Constructor<?> creationConstr = getNmsClass(PACKET_NAME).getDeclaredConstructor(ReflectionTools.getNmsClass("ScoreboardTeam"), int.class);
        creationConstr.setAccessible(true);
        return creationConstr.newInstance(vanilla, 2);
    }

    @SneakyThrows
    private Object createEmptyPacket() {
        Constructor<?> creationConstr = getNmsClass(PACKET_NAME).getDeclaredConstructor();
        creationConstr.setAccessible(true);
        return creationConstr.newInstance();
    }

    @SneakyThrows
    public void markPacket() {
        Field options = handle.getClass().getDeclaredField("j");
        options.setAccessible(true);
        int data = options.getInt(handle);

        final int marker = 66;
        data |= marker << 8;

        options.set(handle, data);
    }

    @SneakyThrows
    public static boolean isMarked(Object packet) {
        Field optionsF = packet.getClass().getDeclaredField("j");
        optionsF.setAccessible(true);
        int options = optionsF.getInt(packet);

        byte rawData = (byte) (options >>> 8);
        return rawData == marker;
    }

    public enum Mode {
        CREATE_TEAM, REMOVE_TEAM, UPDATE_TEAM, ADD_PLAYER, REMOVE_PLAYER
    }

}
