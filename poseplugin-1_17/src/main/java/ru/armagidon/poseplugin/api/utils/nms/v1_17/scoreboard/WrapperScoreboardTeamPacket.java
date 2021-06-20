package ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static net.minecraft.world.scores.Team.Visibility.*;
import static ru.armagidon.poseplugin.api.utils.nms.ReflectionTools.getPropertyField;

public class WrapperScoreboardTeamPacket
{


    public static final int TEAM_CREATED = 0;
    public static final int TEAM_REMOVED = 1;
    public static final int TEAM_UPDATED = 2;
    public static final int PLAYERS_ADDED = 3;
    public static final int PLAYERS_REMOVED = 4;

    public static final ImmutableMap<Team.OptionStatus, net.minecraft.world.scores.Team.CollisionRule> pushBukkitToNotch = ImmutableMap.<Team.OptionStatus, net.minecraft.world.scores.Team.CollisionRule>builder().
            put(Team.OptionStatus.ALWAYS, net.minecraft.world.scores.Team.CollisionRule.ALWAYS).put(Team.OptionStatus.NEVER, net.minecraft.world.scores.Team.CollisionRule.NEVER).put(Team.OptionStatus.FOR_OTHER_TEAMS, net.minecraft.world.scores.Team.CollisionRule.PUSH_OTHER_TEAMS).
            put(Team.OptionStatus.FOR_OWN_TEAM, net.minecraft.world.scores.Team.CollisionRule.PUSH_OWN_TEAM).build();

    public static final ImmutableMap<Team.OptionStatus, net.minecraft.world.scores.Team.Visibility> hideBukkitToNotch = ImmutableMap.<Team.OptionStatus, net.minecraft.world.scores.Team.Visibility>builder().
            put(Team.OptionStatus.ALWAYS, ALWAYS).put(Team.OptionStatus.NEVER, NEVER).put(Team.OptionStatus.FOR_OTHER_TEAMS, HIDE_FOR_OTHER_TEAMS).
            put(Team.OptionStatus.FOR_OWN_TEAM, HIDE_FOR_OWN_TEAM).build();

    private static final byte marker = 12;

    @Getter private int mode = 0;
    @Getter @Setter private List<String> teamMates = new ArrayList<>();

    private final PlayerTeam team;

    public WrapperScoreboardTeamPacket(@NotNull Team team) {
        this(getHandleOfBukkitTeam(team));
    }

    public WrapperScoreboardTeamPacket(@NotNull ClientboundSetPlayerTeamPacket handle) {
        this(createEmptyTeam(handle.getName()));
        retrieveInfo(handle);
    }

    public WrapperScoreboardTeamPacket(@NotNull PlayerTeam team) {
        this.team = team;
    }

    public WrapperScoreboardTeamPacket(String name) {
        this(createEmptyTeam(name));
    }

    //Getters
    public ClientboundSetPlayerTeamPacket getHandle() {

        ClientboundSetPlayerTeamPacket packet;
        try {
            packet = switch (mode) {
                case TEAM_CREATED -> ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
                case TEAM_REMOVED -> ClientboundSetPlayerTeamPacket.createRemovePacket(team);
                case TEAM_UPDATED -> ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false);
                case PLAYERS_ADDED -> ClientboundSetPlayerTeamPacket.createPlayerPacket(team, teamMates.get(0), ClientboundSetPlayerTeamPacket.Action.ADD);
                case PLAYERS_REMOVED -> ClientboundSetPlayerTeamPacket.createPlayerPacket(team, teamMates.get(0), ClientboundSetPlayerTeamPacket.Action.REMOVE);
                default -> null;
            };
        } catch (Exception e) {
            packet = createEmptyPacket();
        }
        markPacket(packet);
        return packet;
    }



    /**
     * Use a the very end. Otherwise all the data will be overwritten
     * */

    public WrapperScoreboardTeamPacket setMode(int mode) {
        this.mode = mode;
        return this;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void setPackOptionData(int optionData, ClientboundSetPlayerTeamPacket packet) {
        Field parametersF = getPropertyField(Optional.class, ClientboundSetPlayerTeamPacket.class);
        parametersF.setAccessible(true);
        Optional<ClientboundSetPlayerTeamPacket.Parameters> parametersOptional = (Optional<ClientboundSetPlayerTeamPacket.Parameters>) parametersF.get(packet);
        if (parametersOptional.isEmpty()) {
            ClientboundSetPlayerTeamPacket.Parameters parameters = new ClientboundSetPlayerTeamPacket.Parameters(team);
            parametersF.set(packet, Optional.of(parameters));
            parametersOptional = (Optional<ClientboundSetPlayerTeamPacket.Parameters>) parametersF.get(packet);
        }
        ClientboundSetPlayerTeamPacket.Parameters parameters = parametersOptional.get();

        Field optionsF = getPropertyField(int.class, ClientboundSetPlayerTeamPacket.Parameters.class);
        optionsF.set(parameters, optionData);
    }

    public void setNameTagVisibility(Team.OptionStatus visibility) {
        team.setNameTagVisibility(hideBukkitToNotch.get(visibility));
    }

    public void setCollisionRule(Team.OptionStatus collisionRule) {
        team.setCollisionRule(pushBukkitToNotch.get(collisionRule));
    }

    public void setSeeFriendlyInvisibles(boolean flag) {
        team.setSeeFriendlyInvisibles(flag);
    }

    public List<String> getPlayers() {
        return teamMates;
    }

    public String getName() {
        return team.getName();
    }

    public int getOptions() {
        return team.packOptions();
    }

    public Team.OptionStatus getNameTagVisibility() {
        return invertMapParams(hideBukkitToNotch).get(team.getNameTagVisibility());
    }

    public Team.OptionStatus getCollisionRule() {
        return invertMapParams(pushBukkitToNotch).get(team.getCollisionRule());
    }

    public boolean canSeeFriendlyInvisibles() {
        return team.canSeeFriendlyInvisibles();
    }

    //Utility methods

    @SneakyThrows
    private static ClientboundSetPlayerTeamPacket createEmptyPacket() {
        Constructor<ClientboundSetPlayerTeamPacket> packetConstructor =
                ClientboundSetPlayerTeamPacket.class.getDeclaredConstructor(String.class, int.class,
                        Optional.class, Collection.class);
        packetConstructor.setAccessible(true);
        return packetConstructor.newInstance("", 0, Optional.empty(), Collections.emptyList());
    }

    public void markPacket(ClientboundSetPlayerTeamPacket packet) {
        int data = team.packOptions();

        data |= marker << 8;  //Shift marker 1 byte to the right

        setPackOptionData(data, packet);
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
        PlayerTeam baseTeam = (PlayerTeam) teamF.get(team);
        PlayerTeam emptyTeam = createEmptyTeam(team.getName());

        Arrays.stream(baseTeam.getClass().getDeclaredFields())
                .filter(field -> (field.getModifiers() & Modifier.STATIC) == 0)
                .filter(field -> !field.getType().equals(Scoreboard.class))
                .peek(field -> field.setAccessible(true))
                .forEach(field -> {
                    try {
                        field.set(emptyTeam, field.get(baseTeam));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        return emptyTeam;
    }

    private static PlayerTeam createEmptyTeam(String name) {
        return new PlayerTeam(new Scoreboard(), name);
    }

    public void sendPacket(Player receiver) {
        ((CraftPlayer)receiver).getHandle().connection.send(getHandle());
    }

    private void retrieveInfo(ClientboundSetPlayerTeamPacket handle) {
        ClientboundSetPlayerTeamPacket.Action teamAction = handle.getTeamAction();
        if (teamAction == null) {
            ClientboundSetPlayerTeamPacket.Action playerAction = handle.getPlayerAction();
            if (playerAction == null)
                this.mode = TEAM_UPDATED;
            else {
                this.mode =  switch (playerAction) {
                    case ADD -> PLAYERS_ADDED;
                    case REMOVE -> PLAYERS_REMOVED;
                };
            }
        } else {
            this.mode =  switch (teamAction) {
                case ADD -> TEAM_CREATED;
                case REMOVE -> TEAM_REMOVED;
            };
        }
        this.teamMates = new ArrayList<>(handle.getPlayers());

        Optional<ClientboundSetPlayerTeamPacket.Parameters> parametersOptional = handle.getParameters();
        parametersOptional.ifPresent(parameters -> {
            team.setPlayerPrefix(parameters.getPlayerPrefix());
            team.setPlayerSuffix(parameters.getPlayerSuffix());
            team.setCollisionRule(net.minecraft.world.scores.Team.CollisionRule.byName(parameters.getCollisionRule()));
            team.setNameTagVisibility(net.minecraft.world.scores.Team.Visibility.byName(parameters.getNametagVisibility()));
            team.setDisplayName(parameters.getDisplayName());
            team.setColor(parameters.getColor());
            int options = parameters.getOptions();
            team.setSeeFriendlyInvisibles(((options & 2) >> 1) == 1);
            team.setAllowFriendlyFire((options & 1) == 1);
        });

    }







}
