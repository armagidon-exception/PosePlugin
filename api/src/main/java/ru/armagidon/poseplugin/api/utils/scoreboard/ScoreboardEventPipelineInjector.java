package ru.armagidon.poseplugin.api.utils.scoreboard;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardEventPipelineInjector
{

    private final String INJECTOR = "ScoreboardEventCatcher";
    private final String POSITION = "packet_handler";
    private final Class<?> PACKET_CLASS = getClassSafely("PacketPlayOutScoreboardTeam");
    private final Set<Object> pendingPackets = ConcurrentHashMap.newKeySet();

    public void inject(Player player){
        try {
            Object vanillaPlayer = NMSUtils.asNMSCopy(player);
            Object connection = vanillaPlayer.getClass().getDeclaredField("playerConnection").get(vanillaPlayer);
            Object networkManager = connection.getClass().getDeclaredField("networkManager").get(connection);
            Channel channel = (Channel) networkManager.getClass().getDeclaredField("channel").get(networkManager);

            channel.pipeline().addBefore(POSITION, INJECTOR, new ChannelDuplexHandler(){
                @Override
                public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                    if (PACKET_CLASS.isInstance(packet)){

                        if (pendingPackets.contains(packet)){
                            pendingPackets.remove(packet);
                            super.write(ctx, packet, promise);
                            return;
                        }

                        Field nameF = packet.getClass().getDeclaredField("a");
                        nameF.setAccessible(true);
                        String name = (String) nameF.get(packet);

                        Field modeF = packet.getClass().getDeclaredField("i");
                        modeF.setAccessible(true);
                        int rawMode = modeF.getInt(packet);

                        EntryScoreboardChangeEvent.Mode mode;
                        switch (rawMode){
                            case 3:
                                mode = EntryScoreboardChangeEvent.Mode.ADD;
                                break;
                            case 4:
                                mode = EntryScoreboardChangeEvent.Mode.REMOVE;
                                break;
                            default:
                                super.write(ctx, packet, promise);
                                return;
                        }

                        Field playersF = packet.getClass().getDeclaredField("h");
                        playersF.setAccessible(true);
                        Collection<String> players = (Collection<String>) playersF.get(packet);

                        Field visibilityF = packet.getClass().getDeclaredField("e");
                        visibilityF.setAccessible(true);
                        String rawVisibility = (String) visibilityF.get(packet);

                        Field collisionF = packet.getClass().getDeclaredField("f");
                        collisionF.setAccessible(true);
                        String rawCollisionRule = (String) collisionF.get(packet);

                        if (players.contains(player.getName())){

                            Team.OptionStatus nameTagVisibility = Team.OptionStatus.values()[mapRuleToOrdinal(rawVisibility)];
                            Team.OptionStatus collisionRule = Team.OptionStatus.values()[mapRuleToOrdinal(rawCollisionRule)];

                            EntryScoreboardChangeEvent event = new EntryScoreboardChangeEvent(player, name, mode, nameTagVisibility, collisionRule);
                            Bukkit.getPluginManager().callEvent(event);
                        }
                    }
                    super.write(ctx, packet, promise);

                }
            });

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private int mapRuleToOrdinal(String rule){
        if (rule.equalsIgnoreCase("always")){
            return 0;
        } else if(rule.equalsIgnoreCase("never")){
            return 1;
        } else if(rule.equalsIgnoreCase("hideForOtherTeams")){
            return 2;
        } else if(rule.equalsIgnoreCase("hideForOwnTeam")){
            return 3;
        } else return -1;
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

    public void sendAndByPassPacket(Player player, Object packet){
        NMSUtils.sendPacket(player, packet);
        pendingPackets.add(packet);
    }

    @SneakyThrows
    private Class<?> getClassSafely(String path){
        return getNmsClass(path);
    }

    private static Class<?> getNmsClass(String nmsClassName) throws Exception {
        return Class.forName("net.minecraft.server." + ReflectionTools.nmsVersion() + "." + nmsClassName);
    }
}
