package ru.armagidon.poseplugin.api.utils.npc.protocolized.New;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayerCustomEquipmentManager;
import ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerEntityEquipment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NewEquipmentManager implements FakePlayerCustomEquipmentManager {

    private final FakePlayer npc;
    private WrapperPlayServerEntityEquipment customEquipmentPacket;
    private final Map<EquipmentSlot, ItemStack> customEquipment;

    public NewEquipmentManager(FakePlayer npc) {
        this.npc = npc;
        this.customEquipment = new HashMap<>();
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
        customEquipmentPacket.setEntityID(npc.getId());
        customEquipmentPacket.setSlotStackPairsList(slots);
        npc.updateNPC();
    }
}
