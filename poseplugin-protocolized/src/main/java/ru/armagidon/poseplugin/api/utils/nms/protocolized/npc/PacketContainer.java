package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.AbstractPacket;

import java.util.Collection;

public class PacketContainer<T extends AbstractPacket> {

    private final T[] packets;

    @SuppressWarnings("unchecked")
    public PacketContainer(Collection<T> packets) {
        this.packets = (T[]) packets.toArray(AbstractPacket[]::new);
    }

    @SafeVarargs
    public PacketContainer(T... packets){
        this.packets = packets;
    }

    public void send(Player receiver) {
        for (T packet : packets) {
            packet.sendPacket(receiver);
        }
    }
}
