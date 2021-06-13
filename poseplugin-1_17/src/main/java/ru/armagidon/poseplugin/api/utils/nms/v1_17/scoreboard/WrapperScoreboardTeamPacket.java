package ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private final ClientboundSetPlayerTeamPacket handle; //TODO make final

    public WrapperScoreboardTeamPacket(Team team) {
        this(ClientboundSetPlayerTeamPacket.createRemovePacket(getHandleOfBukkitTeam(team)));
    }

    public WrapperScoreboardTeamPacket(ClientboundSetPlayerTeamPacket handle) {
        this.handle = handle;
        markPacket();
    }

    public WrapperScoreboardTeamPacket() {
        this(createEmptyPacket());
    }

    @SneakyThrows
    private static ClientboundSetPlayerTeamPacket createEmptyPacket() {
        return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(createEmptyTeam(), false);
    }

    public Team.OptionStatus getNameTagVisibility() {
        return invertMapParams(hideBukkitToNotch).get(handle.getParameters().get().getNametagVisibility());
    }

    public Team.OptionStatus getCollisionRule() {
        return invertMapParams(pushBukkitToNotch).get(handle.getParameters().get().getCollisionRule());
    }


    @SneakyThrows
    public void setPackOptionData(int optionData) {
        Field optionDataF = ClientboundSetPlayerTeamPacket.Parameters.class.getDeclaredField("options");
        optionDataF.setAccessible(true);
        handle.getParameters().ifPresent(data -> {
            try {
                optionDataF.set(data, optionData);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public int getPackOptionData() {
        return handle.getParameters().get().getOptions();
    }

    @SneakyThrows
    public void setNameTagVisibility(Team.OptionStatus nameTagVisibility) {
        Field visibilityF = ClientboundSetPlayerTeamPacket.Parameters.class.getDeclaredField("nametagVisibility");
        visibilityF.setAccessible(true);
        visibilityF.set(handle.getParameters().get(), hideBukkitToNotch.get(nameTagVisibility));
    }

    @SneakyThrows
    public void setCollisionRule(Team.OptionStatus collisionRule) {
        Field collisionRuleF = ClientboundSetPlayerTeamPacket.Parameters.class.getDeclaredField("collisionRule");
        collisionRuleF.setAccessible(true);
        collisionRuleF.set(handle.getParameters().get(), pushBukkitToNotch.get(collisionRule));
    }

    public void setCanSeePlayersInvisibles(boolean flag){
        int curr = getPackOptionData();
        setPackOptionData(setBit((byte) curr, 1, flag));
    }

    public String getName() {
        return handle.getName();
    }

    @SneakyThrows
    public void setName(String name) {
        Field nameF = ClientboundSetPlayerTeamPacket.class.getDeclaredField("name");
        nameF.setAccessible(true);
        nameF.set(handle, name);
    }

    @SneakyThrows
    public void setMode(int mode) {
        Field nameF = ClientboundSetPlayerTeamPacket.class.getDeclaredField("method");
        nameF.setAccessible(true);
        nameF.set(handle, mode);
    }

    public Collection<String> getPlayers() {
        return handle.getPlayers();
    }

    @SneakyThrows
    public int getMode() {
        Field nameF = ClientboundSetPlayerTeamPacket.class.getDeclaredField("method");
        nameF.setAccessible(true);
        return nameF.getInt(handle);
    }

    @SneakyThrows
    public void setTeamMateList(Collection<String> players){
        Field nameF = ClientboundSetPlayerTeamPacket.class.getDeclaredField("players");
        nameF.setAccessible(true);
        nameF.set(handle, players);
    }

    public ClientboundSetPlayerTeamPacket getHandle() {
        return handle;
    }

    public void markPacket() {
        int data = getPackOptionData();

        data |= marker << 8;  //Shift marker 1 byte to the right

        setPackOptionData(data);
    }

    public static boolean isMarked(ClientboundSetPlayerTeamPacket packet) {
        Optional<ClientboundSetPlayerTeamPacket.Parameters> optional = packet.getParameters();
        if (optional.isPresent()) {
            int options = optional.get().getOptions();

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
    private static PlayerTeam getHandleOfBukkitTeam(Team team) {
        var craftTeamClass = Class.forName("org.bukkit.craftbukkit.v1_17_R1.scoreboard.CraftTeam");
        var teamF = craftTeamClass.getDeclaredField("team");
        teamF.setAccessible(true);
        return (PlayerTeam) teamF.get(team);
    }

    private static PlayerTeam createEmptyTeam() {
        return new PlayerTeam(null, "");
    }

    public void sendPacket(Player receiver) {
        ((CraftPlayer)receiver).getHandle().connection.send(handle);
    }

}
