package ru.armagidon.poseplugin.utils.nms.impl._1_15;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.utils.nms.interfaces.FakePlayer;
import ru.armagidon.poseplugin.utils.nms.interfaces.PacketReader;

import java.lang.reflect.Method;
import java.util.List;

public class DamagePacketReader implements PacketReader
{

    private final Player player;
    private Channel channel;

    public DamagePacketReader(Player player) {
        this.player = player;
    }

    public void inject(){
        try {
            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object plrConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            Object nwm = plrConnection.getClass().getField("networkManager").get(plrConnection);
            channel = (Channel) nwm.getClass().getField("channel").get(nwm);
            channel.pipeline().addAfter("decoder", "PackerReader", new MessageToMessageDecoder<>() {
                @Override
                protected void decode(ChannelHandlerContext channelHandlerContext, Object packet, List<Object> list) throws Exception {
                    list.add(packet);
                    readPacket(packet);
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void eject(){
        if(channel!=null&&channel.pipeline().get(player.getName())!=null){
            channel.pipeline().remove(player.getName());
        }
    }

    private void readPacket(Object packet){
        String packetname = packet.getClass().getSimpleName();
        if(packetname.equalsIgnoreCase("PacketPlayInUseEntity")){
            int id = (int) NMSUtils.getValue(packet, "a");
            for (FakePlayer fakePlayer : FakePlayer.FAKE_PLAYERS) {
                if (fakePlayer.getId() == id) {
                    try {
                        Method method = packet.getClass().getDeclaredMethod("b");
                        Object action = method.invoke(packet);
                        Enum actionenum = (Enum) action;
                        if(actionenum.name().equals("ATTACK")){
                            if (fakePlayer.getParent().getGameMode().equals(GameMode.SURVIVAL) || fakePlayer.getParent().getGameMode().equals(GameMode.ADVENTURE)) {
                                damage(fakePlayer);
                                return;
                            }
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void damage(FakePlayer fakePlayer){
        Bukkit.getScheduler().runTask(PosePlugin.getInstance(),()->{
            Bukkit.getOnlinePlayers().forEach(pl -> {
                fakePlayer.animation(pl, (byte) 2);
                pl.playSound(fakePlayer.getParent().getLocation(), Sound.ENTITY_PLAYER_HURT,1,1);
            });
            double damage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
            fakePlayer.getParent().damage(damage,player);
        });
    }
}
