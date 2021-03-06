package ru.armagidon.poseplugin.api.utils.npc.protocolized.old;

import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayerCustomEquipmentManager;
import ru.armagidon.poseplugin.api.utils.npc.protocolized.PacketContainer;
import ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerEntityEquipment;

import java.util.HashMap;
import java.util.Map;

public class OldCustomEquipmentManager implements FakePlayerCustomEquipmentManager
{

    private final FakePlayer fakePlayer;
    private PacketContainer<WrapperPlayServerEntityEquipment> customEquipmentPacket;
    private final Map<EquipmentSlot, ItemStack> customEquipment = new HashMap<>();

    public OldCustomEquipmentManager(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
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
