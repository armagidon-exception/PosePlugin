package ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.world.scores.ScoreboardTeam;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.util.*;

import static ru.armagidon.poseplugin.api.utils.nms.v1_17.npc.NPCMetadataEditor117.setBit;

public class WrapperScoreboardTeamPacket
{


    public static final int TEAM_CREATED = 0;
    public static final int TEAM_REMOVED = 1;
    public static final int TEAM_UPDATED = 2;
    public static final int PLAYERS_ADDED = 3;
    public static final int PLAYERS_REMOVED = 4;

    public static final ImmutableMap<Team.OptionStatus, String> pushBukkitToNotch = ImmutableMap.<Team.OptionStatus, String>builder().
            put(Team.OptionStatus.ALWAYS, "always").put(Team.OptionStatus.NEVER, "never").put(Team.OptionStatus.FOR_OTHER_TEAMS, "pushOtherTeams").
            put(Team.OptionStatus.FOR_OWN_TEAM, "pushOwnTeam").build();

    public static final ImmutableMap<Team.OptionStatus, String> hideBukkitToNotch = ImmutableMap.<Team.OptionStatus, String>builder().
            put(Team.OptionStatus.ALWAYS, "always").put(Team.OptionStatus.NEVER, "never").put(Team.OptionStatus.FOR_OTHER_TEAMS, "hideForOtherTeams").
            put(Team.OptionStatus.FOR_OWN_TEAM, "hideForOwnTeam").build();

    private static final byte marker = 66;

    private final PacketPlayOutScoreboardTeam handle; //TODO make final

    public WrapperScoreboardTeamPacket(Team team) {
        this(PacketPlayOutScoreboardTeam.a(getHandleOfBukkitTeam(team)));
    }

    @SneakyThrows
    public WrapperScoreboardTeamPacket(PacketPlayOutScoreboardTeam handle) {
        this.handle = handle;
        if (handle.f().isEmpty()) {
            Field field = handle.getClass().getDeclaredField("k");
            field.setAccessible(true);
            field.set(handle, Optional.of(new PacketPlayOutScoreboardTeam.b(createEmptyTeam())));
        }
        markPacket();
    }

    public WrapperScoreboardTeamPacket() {
        this(createEmptyPacket());
    }

    @SneakyThrows
    private static PacketPlayOutScoreboardTeam createEmptyPacket() {
        return PacketPlayOutScoreboardTeam.a(createEmptyTeam(), false);
    }

    public Team.OptionStatus getNameTagVisibility() {
        return invertMapParams(hideBukkitToNotch).get(handle.f().get().d());
    }

    public Team.OptionStatus getCollisionRule() {
        return invertMapParams(pushBukkitToNotch).get(handle.f().get().e());
    }


    @SneakyThrows
    public void setPackOptionData(int optionData) {
        Field optionDataF = PacketPlayOutScoreboardTeam.b.class.getDeclaredField("g");
        optionDataF.setAccessible(true);
        handle.f().ifPresent(data -> {
            try {
                optionDataF.set(data, optionData);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public int getPackOptionData() {
        return handle.f().get().b();
    }

    @SneakyThrows
    public void setNameTagVisibility(Team.OptionStatus nameTagVisibility) {
        Field visibilityF = PacketPlayOutScoreboardTeam.b.class.getDeclaredField("d");
        visibilityF.setAccessible(true);
        visibilityF.set(handle.f().get(), hideBukkitToNotch.get(nameTagVisibility));
    }

    @SneakyThrows
    public void setCollisionRule(Team.OptionStatus collisionRule) {
        Field visibilityF = PacketPlayOutScoreboardTeam.b.class.getDeclaredField("e");
        visibilityF.setAccessible(true);
        visibilityF.set(handle.f().get(), pushBukkitToNotch.get(collisionRule));
    }

    public void setCanSeePlayersInvisibles(boolean flag){
        int curr = getPackOptionData();
        setPackOptionData(setBit((byte) curr, 1, flag));
    }

    public String getName() {
        return handle.d();
    }

    @SneakyThrows
    public void setName(String name) {
        Field nameF = PacketPlayOutScoreboardTeam.class.getDeclaredField("i");
        nameF.setAccessible(true);
        nameF.set(handle, name);
    }

    @SneakyThrows
    public void setMode(int mode) {
        Field nameF = PacketPlayOutScoreboardTeam.class.getDeclaredField("h");
        nameF.setAccessible(true);
        nameF.set(handle, mode);
    }

    public Collection<String> getPlayers() {
        return handle.e();
    }

    @SneakyThrows
    public int getMode() {
        Field nameF = PacketPlayOutScoreboardTeam.class.getDeclaredField("h");
        nameF.setAccessible(true);
        return nameF.getInt(handle);
    }

    @SneakyThrows
    public void setTeamMateList(Collection<String> players){
        Field nameF = PacketPlayOutScoreboardTeam.class.getDeclaredField("j");
        nameF.setAccessible(true);
        nameF.set(handle, players);
    }

    public PacketPlayOutScoreboardTeam getHandle() {
        return handle;
    }

    public void markPacket() {
        int data = getPackOptionData();

        data |= marker << 8;  //Shift marker 1 byte to the right

        setPackOptionData(data);
    }

    public static boolean isMarked(PacketPlayOutScoreboardTeam packet) {
        Optional<PacketPlayOutScoreboardTeam.b> optional = packet.f();
        if (optional.isPresent()) {
            int options = optional.get().b();

            byte rawData = (byte) (options >>> 8); //Restore marker value and clear it out of any other markers
            return rawData == marker;
        }
        return false;
    }

    public static <K, V> Map<V, K> invertMapParams(Map<K, V> map) {
        Map<V, K> output = new HashMap<>();
        map.forEach((key, value) -> output.put(value, key));
        return output;
    }

    @SneakyThrows
    private static ScoreboardTeam getHandleOfBukkitTeam(Team team) {
        var craftTeamClass = Class.forName("org.bukkit.craftbukkit.v1_17_R1.scoreboard.CraftTeam");
        var teamF = craftTeamClass.getDeclaredField("team");
        teamF.setAccessible(true);
        return (ScoreboardTeam) teamF.get(team);
    }

    private static ScoreboardTeam createEmptyTeam() {
        return new ScoreboardTeam(null, "");
    }

    public void sendPacket(Player receiver) {
        ((CraftPlayer)receiver).getHandle().b.sendPacket(handle);
    }

}
