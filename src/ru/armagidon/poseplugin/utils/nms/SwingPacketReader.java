package ru.armagidon.poseplugin.utils.nms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;

public class SwingPacketReader extends PacketReader
{


    public SwingPacketReader(Player player) {
        super(player, "SwingPacketReader");
    }

    protected void readPackets(Player sender, Object packet) throws Exception{
        String packetname = packet.getClass().getSimpleName();
        if(packetname.equalsIgnoreCase("packetplayinarmanimation")){
            //Swing hand
            if(PosePlugin.getInstance().containsPlayer(sender)) {
                PosePluginPlayer p = PosePlugin.getInstance().getPosePluginPlayer(sender.getName());
                if (p.getPoseType().equals(EnumPose.LYING)) {
                    Object enumhandObject = packet.getClass().getDeclaredMethod("b").invoke(packet);
                    Enum enumHand = (Enum) enumhandObject;
                    boolean mainHand = enumHand.name().equalsIgnoreCase("main_hand");
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        FakePlayer.FAKE_PLAYERS.get(sender).swingHand(onlinePlayer, mainHand);
                    }
                }
            }

        }
    }
}
