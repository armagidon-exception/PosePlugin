package ru.armagidon.poseplugin.api.utils.nms.v1_17.npc;

import com.mojang.datafixers.util.Pair;
import lombok.SneakyThrows;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCInventory;

import java.util.List;
import java.util.stream.Collectors;


public class NPCInventory117 extends NPCInventory<SynchedEntityData> {

    private ClientboundSetEquipmentPacket customEquipmentPacket;

    public NPCInventory117(FakePlayer117 npc) {
        super(npc);
    }

    public void showEquipment(Player receiver){
        if(!customEquipment.isEmpty())
            FakePlayer117.sendPacket(receiver, customEquipmentPacket);
    }

    public void mergeCustomEquipmentPacket() {
        List<Pair<net.minecraft.world.entity.EquipmentSlot, ItemStack>> slots = customEquipment.entrySet().stream().map(entry->
                Pair.of(adaptToItemSlot(entry.getKey()), CraftItemStack.asNMSCopy(entry.getValue()))).collect(Collectors.toList());
        customEquipmentPacket = new ClientboundSetEquipmentPacket(fakePlayer.getId(), slots);
    }

    @SneakyThrows
    public static net.minecraft.world.entity.EquipmentSlot adaptToItemSlot(EquipmentSlot slotType){
        return net.minecraft.world.entity.EquipmentSlot.values()[slotType.ordinal()];
    }
}
