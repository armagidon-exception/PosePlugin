package ru.armagidon.poseplugin.api.utils.scoreboard;

import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;
import ru.armagidon.poseplugin.api.utils.scoreboard.WrapperScoreboardTeamPacket.Mode;

import java.lang.reflect.Field;
import java.util.Collection;

import static ru.armagidon.poseplugin.api.utils.scoreboard.WrapperScoreboardTeamPacket.isMarked;

public class ScoreboardEventPipelineInjector
{

    private final String INJECTOR = "PPScorebrdInjector";

    private static final Class<?> PACKET_CLASS = getClassSafely("PacketPlayOutScoreboardTeam");


    public void inject(Player player){
        try {
            Object vanillaPlayer = NMSUtils.asNMSCopy(player);
            Object connection = vanillaPlayer.getClass().getDeclaredField("playerConnection").get(vanillaPlayer);
            Object networkManager = connection.getClass().getDeclaredField("networkManager").get(connection);
            Channel channel = (Channel) networkManager.getClass().getDeclaredField("channel").get(networkManager);

            //Catch events
            final String POSITION = "packet_handler";
            channel.pipeline().addBefore(POSITION, INJECTOR, new ChannelOutboundHandlerAdapter() {
                @Override
                public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                    PacketData packetData = PacketData.toPacketData(packet);
                    if (packetData != null) {
                        if (!isMarked(packet)) {
                            if (packetData.getMode() == Mode.ADD_PLAYER || packetData.getMode() == Mode.REMOVE_PLAYER) {
                                if (!isMarked(packet)) {
                                    if (packetData.getTeamMates().contains(player.getName())) {
                                        Bukkit.getPluginManager().callEvent(new ScoreboardTeamChangeEvent(player, packet, packetData,
                                                isMarked(packet)));
                                    }
                                }
                            } else if (packetData.getMode() == Mode.UPDATE_TEAM) {
                                ScoreboardTeamChangeEvent event = new ScoreboardTeamChangeEvent(player, packet, packetData,
                                        isMarked(packet));
                                Bukkit.getPluginManager().callEvent(event);
                                super.write(ctx, event.getPacket(), promise);
                                return;
                            }
                        }
                    }
                    super.write(ctx, packet, promise);
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    public void eject(Player player){
        try {
            Object vanillaPlayer = NMSUtils.asNMSCopy(player);
            Object connection = vanillaPlayer.getClass().getDeclaredField("playerConnection").get(vanillaPlayer);
            Object networkManager = connection.getClass().getDeclaredField("networkManager").get(connection);
            Channel channel = (Channel) networkManager.getClass().getDeclaredField("channel").get(networkManager);

            if (channel.pipeline().names().contains(INJECTOR)){
                channel.pipeline().remove(INJECTOR);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @SneakyThrows
    private static Class<?> getClassSafely(String path){
        return ReflectionTools.getNmsClass(path);
    }


    @AllArgsConstructor
    @Getter
    public static class PacketData {
        private final Mode mode;
        private final String teamName;
        private final Team.OptionStatus visibility;
        private final Team.OptionStatus collision;
        private final Collection<String> teamMates;

        static PacketData toPacketData(Object packet) throws Exception{
            if (!PACKET_CLASS.isInstance(packet)) return null;

            Field nameF = packet.getClass().getDeclaredField("a");
            nameF.setAccessible(true);
            String name = (String) nameF.get(packet);

            Field modeF = packet.getClass().getDeclaredField("i");
            modeF.setAccessible(true);
            int rawMode = modeF.getInt(packet);

            Mode mode;
            switch (rawMode) {
                case 2:
                    mode = Mode.UPDATE_TEAM;
                    break;
                case 3:
                    mode = Mode.ADD_PLAYER;
                    break;
                case 4:
                    mode = Mode.REMOVE_PLAYER;
                    break;
                default:
                    return null;
            }
            Field playersF = packet.getClass().getDeclaredField("h");
            playersF.setAccessible(true);
            Collection<String> players = mode == Mode.UPDATE_TEAM ?
                    Lists.newArrayList() : (Collection<String>) playersF.get(packet);

            Field visibilityF = packet.getClass().getDeclaredField("e");
            visibilityF.setAccessible(true);
            String rawVisibility = (String) visibilityF.get(packet);

            Field collisionF = packet.getClass().getDeclaredField("f");
            collisionF.setAccessible(true);
            String rawCollisionRule = (String) collisionF.get(packet);

            Team.OptionStatus nameTagVisibility = Team.OptionStatus.values()[mapRuleToOrdinal(rawVisibility)];
            Team.OptionStatus collisionRule = Team.OptionStatus.values()[mapRuleToOrdinal(rawCollisionRule)];

            return new PacketData(mode, name, nameTagVisibility, collisionRule, players);

        }

        private static int mapRuleToOrdinal(String rule){
            if (rule.equalsIgnoreCase("always"))
                return 0;
            else if(rule.equalsIgnoreCase("never"))
                return 1;
            else if(rule.equalsIgnoreCase("hideForOtherTeams"))
                return 2;
            else if(rule.equalsIgnoreCase("hideForOwnTeam"))
                return 3;
            else if (rule.equalsIgnoreCase("pushOwnTeam"))
                return 3;
            else if (rule.equalsIgnoreCase("pushOtherTeams"))
                return 2;
            else
                return -1;
        }

        @Override
        public String toString() {
            return "PacketData{" +
                    "mode=" + mode +
                    ", teamName='" + teamName + '\'' +
                    ", visibility=" + visibility +
                    ", collision=" + collision +
                    ", teamMates=" + teamMates +
                    '}';
        }
    }

}
