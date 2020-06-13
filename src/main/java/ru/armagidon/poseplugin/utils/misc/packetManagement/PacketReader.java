package ru.armagidon.poseplugin.utils.misc.packetManagement;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.PosePlugin;

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
            Channel channel = ((CraftPlayer)sender).getHandle().playerConnection.networkManager.channel;
            if(channel.pipeline().get(getReaderName())==null) {
                channel.pipeline().addBefore("packet_handler", getReaderName(), new ChannelDuplexHandler() {

                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        try {
                            if(!readServerPackets(sender, msg)){
                                return;
                            }
                        } catch (Exception e) {
                            PosePlugin.getInstance().getLogger().severe("Error in packetReader: " + e.getMessage());
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
                            PosePlugin.getInstance().getLogger().severe("Error in packetReader: " + e.getMessage());
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
        Channel channel = ((CraftPlayer)sender).getHandle().playerConnection.networkManager.channel;
        if(channel!=null&&channel.pipeline().get(getReaderName())!=null){
            channel.pipeline().remove(getReaderName());
        }
    }

    public boolean containsInPipeline(Player player){
        return ((CraftPlayer)player).getHandle().playerConnection.networkManager.channel.pipeline().get(getReaderName())!=null;
    }

    protected abstract boolean readServerPackets(Player sender, Object packet) throws Exception;

    protected abstract boolean readClientPackets(Player sender, Object packet) throws Exception;
}
