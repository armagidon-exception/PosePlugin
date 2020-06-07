package ru.armagidon.poseplugin.utils.nms;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.PosePlugin;

public abstract class PacketReader {

    private Channel channel;
    private final String packetName;
    private Player sender;

    public PacketReader(Player sender, String packetName) {
        this.sender = sender;
        this.packetName = packetName;
    }

    public String getPacketName() {
        return packetName;
    }

    public void inject() {

        try {
            Object nmsPlayer = sender.getClass().getMethod("getHandle").invoke(sender);
            Object plrConnection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
            Object nwm = plrConnection.getClass().getField("networkManager").get(plrConnection);
            channel = (Channel) nwm.getClass().getField("channel").get(nwm);
            if (channel.pipeline().get(getPacketName()) == null) {
                channel.pipeline().addBefore("packet_handler", getPacketName(), new ChannelDuplexHandler() {

                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        try {
                            readPackets(sender, msg);
                        } catch (Exception e) {
                            PosePlugin.getInstance().getLogger().severe("Error in packetReader: " + e.getMessage());
                        }
                        if (msg != null) super.channelRead(ctx, msg);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void eject() {
        if (channel != null && channel.pipeline().get(getPacketName()) != null) {
            channel.pipeline().remove(getPacketName());
        }
    }

    protected abstract void readPackets(Player sender, Object packet) throws Exception;
}
