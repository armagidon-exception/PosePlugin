package ru.armagidon.poseplugin.api.utils.npc.protocolized;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.wrappers.AbstractPacket;

public class PacketContainer<T extends AbstractPacket>{
    private final T[] packets;

    @SafeVarargs
    public PacketContainer(T... packets) {
        this.packets = packets;
    }

    public void send(Player receiver){
        for (T packet : packets) {
            packet.sendPacket(receiver);
        }
    }
}
