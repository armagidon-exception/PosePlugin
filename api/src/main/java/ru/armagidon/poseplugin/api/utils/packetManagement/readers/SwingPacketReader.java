package ru.armagidon.poseplugin.api.utils.packetManagement.readers;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.packetManagement.PacketReader;

public final class SwingPacketReader extends PacketReader
{


    public SwingPacketReader() {
        super( "SwingPacketReader");
    }

    @Override
    protected boolean readServerPackets(Player sender, Object packet) throws Exception {
        String packetname = packet.getClass().getSimpleName();
        if(packetname.equalsIgnoreCase("packetplayinarmanimation")){
            //Swing hand

            if(PosePluginAPI.getAPI().getPlayerMap().containsPlayer(sender)) {
                PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(sender.getName());
                if (p.getPoseType().equals(EnumPose.LYING)) {
                    Object enumhandObject = packet.getClass().getDeclaredMethod("b").invoke(packet);
                    Enum<?> enumHand = (Enum<?>) enumhandObject;
                    boolean mainHand = enumHand.name().equalsIgnoreCase("main_hand");
                    FakePlayer.FAKE_PLAYERS.get(sender).swingHand(mainHand);

                }
            }
        }
        return true;
    }

    @Override
    protected boolean readClientPackets(Player sender, Object packet) {
        //Swing reader doesn't read client packets
        return true;
    }
}
