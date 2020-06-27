package ru.armagidon.poseplugin.utils.misc.packetManagement;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.utils.nms.ReflectionTools;

import java.lang.reflect.InvocationTargetException;

public abstract class PacketReader {

    private final String packetName;

    public PacketReader(String packetName) {
        this.packetName = packetName;
    }

    public String getReaderName() {
        return packetName;
    }

    public void inject(Player sender){

        try {
            Channel channel = NMSUtils.getPlayersChannel(sender);
            if(channel.pipeline().get(getReaderName())==null) {
                channel.pipeline().addBefore("packet_handler", getReaderName(), new ChannelDuplexHandler() {

                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        try {
                            if(!readServerPackets(sender, msg)){
                                return;
                            }
                        } catch (Exception e) {
                            ReflectionTools.getPlugin().getLogger().severe("Error in packetReader: " + e.getMessage());
                        }
                        if (msg != null) super.channelRead(ctx, msg);
                    }

                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        try{
                            if(!readClientPackets(sender, msg)){
                                return;
                            }
                        } catch (Exception e){
                            ReflectionTools.getPlugin().getLogger().severe("Error in packetReader: " + e.getMessage());
                        }
                        if(msg!=null) super.write(ctx, msg, promise);
                    }
                });
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }
    public void eject(Player sender){
        Channel channel = null;
        try {
            channel = NMSUtils.getPlayersChannel(sender);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        if(channel!=null&&channel.pipeline().get(getReaderName())!=null){
            channel.pipeline().remove(getReaderName());
        }
    }

    public boolean containsInPipeline(Player player){
        try {
            return NMSUtils.getPlayersChannel(player).pipeline().get(getReaderName())!=null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected abstract boolean readServerPackets(Player sender, Object packet) throws Exception;

    protected abstract boolean readClientPackets(Player sender, Object packet) throws Exception;
}
