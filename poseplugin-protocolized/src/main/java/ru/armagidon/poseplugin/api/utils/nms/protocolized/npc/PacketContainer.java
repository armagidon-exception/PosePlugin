package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.AbstractPacket;

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
