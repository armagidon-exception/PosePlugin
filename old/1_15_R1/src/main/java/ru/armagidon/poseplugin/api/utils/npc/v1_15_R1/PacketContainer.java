package ru.armagidon.poseplugin.api.utils.npc.v1_15_R1;

import net.minecraft.server.v1_15_R1.Packet;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;

@Deprecated
public class PacketContainer<T extends Packet<?>>{
    private final T[] packets;

    @SafeVarargs
    public PacketContainer(T... packets) {
        this.packets = packets;
    }

    public void send(Player receiver){
        for (T packet : packets) {
            NMSUtils.sendPacket(receiver, packet);
        }
    }
}
