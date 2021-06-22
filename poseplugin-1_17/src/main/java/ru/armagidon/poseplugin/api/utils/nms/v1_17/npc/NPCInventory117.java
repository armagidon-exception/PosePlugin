package ru.armagidon.poseplugin.api.utils.nms.v1_17.npc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCInventory;

import java.util.List;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayerUtils.adaptToItemSlot;


public class NPCInventory117 extends NPCInventory<SynchedEntityData> {

    private ClientboundSetEquipmentPacket customEquipmentPacket;

    public NPCInventory117(FakePlayer117 npc) {
        super(npc);
    }

    public void show(Player receiver){
        FakePlayer117.sendPacket(receiver, customEquipmentPacket);
    }

    public void mergeCustomEquipmentPacket() {
        List<Pair<EquipmentSlot, ItemStack>> slots = getCustomEquipment().getAll().stream().map(entry->
                Pair.of(adaptToItemSlot(entry.getKey(), EquipmentSlot.class), CraftItemStack.asNMSCopy(entry.getValue()))).collect(Collectors.toList());
        customEquipmentPacket = new ClientboundSetEquipmentPacket(fakePlayer.getId(), slots);
    }


    @Override
    public void update() {
        List<Pair<EquipmentSlot, ItemStack>> slots = getCustomEquipment().getDirty().stream().map(entry->
                Pair.of(adaptToItemSlot(entry.getKey(), EquipmentSlot.class), CraftItemStack.asNMSCopy(entry.getValue()))).collect(Collectors.toList());
        if (slots.isEmpty()) return;
        ClientboundSetEquipmentPacket updatePacket = new ClientboundSetEquipmentPacket(fakePlayer.getId(), slots);

        fakePlayer.getTrackers().forEach(tracker -> FakePlayer117.sendPacket(tracker, updatePacket));

    }
}
