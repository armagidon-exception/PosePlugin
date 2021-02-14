package ru.armagidon.poseplugin.api.utils.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.collect.ImmutableMap;
import org.bukkit.scoreboard.Team;
import ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerScoreboardTeam;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.armagidon.poseplugin.api.utils.npc.FakePlayerUtils.setBit;

public class WrapperScoreboardTeamPacket
{

    public static final ImmutableMap<Team.OptionStatus, String> pushBukkitToNotch = ImmutableMap.<Team.OptionStatus, String>builder().
            put(Team.OptionStatus.ALWAYS, "always").put(Team.OptionStatus.NEVER, "never").put(Team.OptionStatus.FOR_OTHER_TEAMS, "pushOtherTeams").
            put(Team.OptionStatus.FOR_OWN_TEAM, "pushOwnTeam").build();

    public static final ImmutableMap<Team.OptionStatus, String> hideBukkitToNotch = ImmutableMap.<Team.OptionStatus, String>builder().
            put(Team.OptionStatus.ALWAYS, "always").put(Team.OptionStatus.NEVER, "never").put(Team.OptionStatus.FOR_OTHER_TEAMS, "hideForOtherTeams").
            put(Team.OptionStatus.FOR_OWN_TEAM, "hideForOwnTeam").build();

    private static final byte marker = 66;

    private final WrapperPlayServerScoreboardTeam handle;

    public WrapperScoreboardTeamPacket(Team team) {
        this.handle = createPacket(team);
        markPacket();
    }

    public WrapperScoreboardTeamPacket(WrapperPlayServerScoreboardTeam handle) {
        this.handle = handle;
        markPacket();
    }

    public WrapperScoreboardTeamPacket() {
        handle = createEmptyPacket();
        markPacket();
    }

    public Team.OptionStatus getNameTagVisibility() {
        return invertMapParams(hideBukkitToNotch).get(handle.getNameTagVisibility());
    }

    public Team.OptionStatus getCollisionRule() {
        return invertMapParams(pushBukkitToNotch).get(handle.getCollisionRule());
    }


    public void setNameTagVisibility(Team.OptionStatus nameTagVisibility) {
        handle.setNameTagVisibility(hideBukkitToNotch.get(nameTagVisibility));
    }

    public void setCollisionRule(Team.OptionStatus collisionRule) {
        handle.setCollisionRule(pushBukkitToNotch.get(collisionRule));
    }

    public void setCanSeePlayersInvisibles(boolean flag){
        int curr = handle.getPackOptionData();
        handle.setPackOptionData(setBit((byte) curr, 1, flag));
    }

    public void setName(String name) {
        handle.setName(name);
    }

    public void setMode(int mode) {
        handle.setMode(mode);
    }

    public void setTeamMateList(List<String> players){
        handle.setPlayers(players);
    }

    public WrapperPlayServerScoreboardTeam getHandle() {
        return handle;
    }

    private WrapperPlayServerScoreboardTeam createPacket(Team team) {
        Object vanilla;
        try {
            Field teamF = team.getClass().getDeclaredField("team");
            teamF.setAccessible(true);
            vanilla = teamF.get(team);
        } catch (Exception e) {
            return createEmptyPacket();
        }

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacketConstructor(PacketType.Play.Server.SCOREBOARD_TEAM, vanilla, 2)
                .createPacket(vanilla, 2);
        return new WrapperPlayServerScoreboardTeam(packet);
    }

    private WrapperPlayServerScoreboardTeam createEmptyPacket() {
        return new WrapperPlayServerScoreboardTeam();
    }

    public void markPacket() {
        int data = handle.getPackOptionData();

        final int marker = 66;
        data |= marker << 8;  //Shift marker 1 byte to the right

        handle.setPackOptionData(data);
    }

    public static boolean isMarked(WrapperPlayServerScoreboardTeam packet) {
        int options = packet.getPackOptionData();

        byte rawData = (byte) (options >>> 8); //Restore marker value and clear it out of any other markers
        return rawData == marker;
    }

    private static <K, V> Map<V, K> invertMapParams(Map<K, V> map) {
        Map<V, K> output = new HashMap<>();
        map.forEach((key, value) -> output.put(value, key));
        return output;
    }

}
