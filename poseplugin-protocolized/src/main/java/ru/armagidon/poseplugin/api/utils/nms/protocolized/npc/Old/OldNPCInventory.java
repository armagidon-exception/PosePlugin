package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.Old;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
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

    @Override
    public void setPieceOfEquipment(EquipmentSlot slotType, ItemStack hand) {
        if(hand == null) return;
        customEquipment.put(slotType, hand);
        mergeCustomEquipmentPacket();
    }

    @Override
    public void removePieceOfEquipment(EquipmentSlot slot) {
        if(slot == null) return;
        if( !customEquipment.containsKey(slot) ) return;
        customEquipment.remove(slot);
        mergeCustomEquipmentPacket();
    }

    private void mergeCustomEquipmentPacket() {
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
        fakePlayer.updateNPC();
    }
}
