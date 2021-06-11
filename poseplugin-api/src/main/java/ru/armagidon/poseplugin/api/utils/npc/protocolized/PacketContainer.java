package ru.armagidon.poseplugin.api.utils.npc.protocolized;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.wrappers.AbstractPacket;

public record PacketContainer<T extends AbstractPacket>(T... packets) {
    @SafeVarargs
    public PacketContainer {
    }

    public void send(Player receiver) {
        for (T packet : packets) {
            packet.sendPacket(receiver);
        }
    }
}
