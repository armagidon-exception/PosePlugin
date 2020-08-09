package ru.armagidon.poseplugin.api.utils.nms.npc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R1.EnumItemSlot;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.PacketPlayOutEntityEquipment;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomEquipmentInterfaceImpl_v1_16_R1 implements CustomEquipmentInterface
{
    private final FakePlayer_v1_16_R1 npc;
    private PacketPlayOutEntityEquipment customEquipmentPacket;
    private final Map<EnumItemSlot, ItemStack> customEquipment;

    public CustomEquipmentInterfaceImpl_v1_16_R1(FakePlayer_v1_16_R1 npc) {
        this.npc = npc;
        this.customEquipment = new HashMap<>();
    }

    public void showEquipment(Player receiver){
        if(!customEquipment.isEmpty())
            NMSUtils.sendPacket(receiver, customEquipmentPacket);
    }

    public void setItemInMainHand(org.bukkit.inventory.ItemStack hand) {
        if(hand!=null){
            customEquipment.put(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(hand));
            mergeCustomEquipmentPacket();
        }
    }

    public void setItemInOffHand(org.bukkit.inventory.ItemStack hand) {
        if(hand!=null){
            customEquipment.put(EnumItemSlot.OFFHAND, CraftItemStack.asNMSCopy(hand));
            mergeCustomEquipmentPacket();
        }
    }

    private void mergeCustomEquipmentPacket() {
        List<Pair<EnumItemSlot, ItemStack>> slots = customEquipment.entrySet().stream().map(entry->
                Pair.of(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        customEquipmentPacket = new PacketPlayOutEntityEquipment(npc.getFake().getId(), slots);
        npc.updateNPC();
    }

}
