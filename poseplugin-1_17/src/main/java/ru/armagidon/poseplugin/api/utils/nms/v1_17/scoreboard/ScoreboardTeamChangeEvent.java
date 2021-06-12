package ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Field;
import java.util.Collection;

import static ru.armagidon.poseplugin.api.utils.nms.v1_17.npc.NPCMetadataEditor117.setBit;
import static ru.armagidon.poseplugin.api.utils.nms.v1_17.scoreboard.WrapperScoreboardTeamPacket.*;

@Getter
@AllArgsConstructor
public class ScoreboardTeamChangeEvent extends Event
{

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final PacketPlayOutScoreboardTeam packet;

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }


    public Team.OptionStatus getNameTagVisibility() {
        return invertMapParams(hideBukkitToNotch).get(packet.f().get().d());
    }

    public Team.OptionStatus getCollisionRule() {
        return invertMapParams(pushBukkitToNotch).get(packet.f().get().e());
    }


    @SneakyThrows
    public void setPackOptionData(int optionData) {
        Field optionDataF = PacketPlayOutScoreboardTeam.b.class.getDeclaredField("g");
        optionDataF.setAccessible(true);
        packet.f().ifPresent(data -> {
            try {
                optionDataF.set(data, optionData);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public int getPackOptionData() {
        return packet.f().get().b();
    }

    @SneakyThrows
    public void setNameTagVisibility(Team.OptionStatus nameTagVisibility) {
        Field visibilityF = PacketPlayOutScoreboardTeam.b.class.getDeclaredField("d");
        visibilityF.setAccessible(true);
        visibilityF.set(packet.f().get(), hideBukkitToNotch.get(nameTagVisibility));
    }

    @SneakyThrows
    public void setCollisionRule(Team.OptionStatus collisionRule) {
        Field visibilityF = PacketPlayOutScoreboardTeam.b.class.getDeclaredField("e");
        visibilityF.setAccessible(true);
        visibilityF.set(packet.f().get(), pushBukkitToNotch.get(collisionRule));
    }

    public void setCanSeePlayersInvisibles(boolean flag){
        int curr = getPackOptionData();
        setPackOptionData(setBit((byte) curr, 1, flag));
    }

    public String getName() {
        return packet.d();
    }

    @SneakyThrows
    public void setName(String name) {
        Field nameF = PacketPlayOutScoreboardTeam.class.getDeclaredField("i");
        nameF.setAccessible(true);
        nameF.set(packet, name);
    }

    @SneakyThrows
    public void setMode(int mode) {
        Field nameF = PacketPlayOutScoreboardTeam.class.getDeclaredField("h");
        nameF.setAccessible(true);
        nameF.set(packet, mode);
    }

    public Collection<String> getPlayers() {
        return packet.e();
    }

    @SneakyThrows
    public int getMode() {
        Field nameF = PacketPlayOutScoreboardTeam.class.getDeclaredField("h");
        nameF.setAccessible(true);
        return nameF.getInt(packet);
    }

    @SneakyThrows
    public void setTeamMateList(Collection<String> players){
        Field nameF = PacketPlayOutScoreboardTeam.class.getDeclaredField("j");
        nameF.setAccessible(true);
        nameF.set(packet, players);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
