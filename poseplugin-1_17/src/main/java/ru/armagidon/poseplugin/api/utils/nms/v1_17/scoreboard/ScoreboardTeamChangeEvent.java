package ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ru.armagidon.poseplugin.api.utils.nms.v1_17.npc.NPCMetadataEditor117.setBit;
import static ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard.WrapperScoreboardTeamPacket.*;

@Getter
@AllArgsConstructor
public class ScoreboardTeamChangeEvent extends Event
{

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final ClientboundSetPlayerTeamPacket handle;

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
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

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public ClientboundSetPlayerTeamPacket getPacket() {
        return handle;
    }
}
