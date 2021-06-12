package ru.armagidon.poseplugin.api.utils.nms.v1_17.npc;

import com.mojang.datafixers.util.Pair;
import lombok.SneakyThrows;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCInventory;

import java.util.List;
import java.util.stream.Collectors;


public class NPCInventory117 extends NPCInventory<DataWatcher> {

    private PacketPlayOutEntityEquipment customEquipmentPacket;

    public NPCInventory117(FakePlayer117 npc) {
        super(npc);
    }

    public void showEquipment(Player receiver){
        if(!customEquipment.isEmpty())
            FakePlayer117.sendPacket(receiver, customEquipmentPacket);
    }

    @Override
    public void setPieceOfEquipment(EquipmentSlot slotType, org.bukkit.inventory.ItemStack hand) {
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
        List<Pair<EnumItemSlot, ItemStack>> slots = customEquipment.entrySet().stream().map(entry->
                Pair.of(adaptToItemSlot(entry.getKey()), CraftItemStack.asNMSCopy(entry.getValue()))).collect(Collectors.toList());
        customEquipmentPacket = new PacketPlayOutEntityEquipment(fakePlayer.getId(), slots);
        fakePlayer.updateNPC();
    }

    @SneakyThrows
    public static EnumItemSlot adaptToItemSlot(EquipmentSlot slotType){
        return EnumItemSlot.values()[slotType.ordinal()];
    }

}
