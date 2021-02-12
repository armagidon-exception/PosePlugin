package ru.armagidon.poseplugin.api.utils.npc.v1_16_R2;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R2.EnumItemSlot;
import net.minecraft.server.v1_16_R2.ItemStack;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityEquipment;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayerCustomEquipmentManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.npc.FakePlayerUtils.adaptToItemSlot;

public class CustomEquipmentManagerImpl implements FakePlayerCustomEquipmentManager {
    private final FakePlayer npc;
    private PacketPlayOutEntityEquipment customEquipmentPacket;
    private final Map<EnumItemSlot, net.minecraft.server.v1_16_R2.ItemStack> customEquipment;

    public CustomEquipmentManagerImpl(FakePlayer npc) {
        this.npc = npc;
        this.customEquipment = new HashMap<>();
    }

    public void showEquipment(Player receiver){
        if(!customEquipment.isEmpty())
            NMSUtils.sendPacket(receiver, customEquipmentPacket);
    }

    @Override
    public void setPieceOfEquipment(EquipmentSlot slotType, org.bukkit.inventory.ItemStack hand) {
        if(hand == null) return;
        ItemStack vanillaStack = CraftItemStack.asNMSCopy(hand);
        customEquipment.put((EnumItemSlot) adaptToItemSlot(slotType), vanillaStack);
        mergeCustomEquipmentPacket();
    }

    @Override
    public void removePieceOfEquipment(EquipmentSlot slot) {
        if(slot ==  null) return;
        EnumItemSlot itemSlot = (EnumItemSlot) adaptToItemSlot(slot);
        if(!customEquipment.containsKey(itemSlot)) return;
        customEquipment.remove(itemSlot);
        mergeCustomEquipmentPacket();
    }

    private void mergeCustomEquipmentPacket() {
        List<Pair<EnumItemSlot, ItemStack>> slots = customEquipment.entrySet().stream().map(entry->
                Pair.of(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        customEquipmentPacket = new PacketPlayOutEntityEquipment(npc.getFake().getId(), slots);
        npc.updateNPC();
    }

}
