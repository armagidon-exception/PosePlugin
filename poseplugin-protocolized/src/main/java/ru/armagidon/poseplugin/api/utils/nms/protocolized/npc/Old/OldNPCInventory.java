package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.Old;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCInventory;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.PacketContainer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityEquipment;

public class OldNPCInventory extends NPCInventory<WrappedDataWatcher>
{

    private PacketContainer<WrapperPlayServerEntityEquipment> customEquipmentPacket;

    public OldNPCInventory(FakePlayer<WrappedDataWatcher> fakePlayer) {
        super(fakePlayer);
    }

    public void showEquipment(Player receiver){
        if(!customEquipment.isEmpty())
            customEquipmentPacket.send(receiver);
    }

    public void mergeCustomEquipmentPacket() {
        WrapperPlayServerEntityEquipment[] eq = customEquipment.entrySet().stream().
                map(entry -> {
                    WrapperPlayServerEntityEquipment equipmentPacket = new WrapperPlayServerEntityEquipment();
                    equipmentPacket.setEntityID(fakePlayer.getId());
                    equipmentPacket.setSlot(EnumWrappers.ItemSlot.values()[entry.getKey().ordinal()]);
                    equipmentPacket.setItem(entry.getValue());
                    return equipmentPacket;
                })
                .toArray(WrapperPlayServerEntityEquipment[]::new);
        customEquipmentPacket = new PacketContainer<>(eq);
    }
}
