package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.New;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCInventory;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityEquipment;

import java.util.List;
import java.util.stream.Collectors;

public class NewNPCInventory extends NPCInventory<WrappedDataWatcher> {

    private WrapperPlayServerEntityEquipment customEquipmentPacket;

    public NewNPCInventory(FakePlayer<WrappedDataWatcher> fakePlayer) {
        super(fakePlayer);
    }

    public void showEquipment(Player receiver){
        if(!customEquipment.isEmpty())
             customEquipmentPacket.sendPacket(receiver);
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
        if(!customEquipment.containsKey(slot)) return;
        customEquipment.remove(slot);
        mergeCustomEquipmentPacket();
    }

    private void mergeCustomEquipmentPacket() {
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slots = customEquipment.entrySet().stream().map(entry ->
                new Pair<>(EnumWrappers.ItemSlot.values()[entry.getKey().ordinal()], entry.getValue())).collect(Collectors.toList());
        customEquipmentPacket = new WrapperPlayServerEntityEquipment();
        customEquipmentPacket.setEntityID(fakePlayer.getId());
        customEquipmentPacket.setSlotStackPairsList(slots);
        fakePlayer.updateNPC();
    }
}
