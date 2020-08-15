package ru.armagidon.poseplugin.api.utils.nms.npc;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R2.EnumItemSlot;
import net.minecraft.server.v1_16_R2.ItemStack;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityEquipment;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomEquipmentInterfaceImpl_v1_16_R2 implements CustomEquipmentInterface
{
    private final FakePlayer_v1_16_R2 npc;
    private PacketPlayOutEntityEquipment customEquipmentPacket;
    private final Map<EnumItemSlot, net.minecraft.server.v1_16_R2.ItemStack> customEquipment;

    public CustomEquipmentInterfaceImpl_v1_16_R2(FakePlayer_v1_16_R2 npc) {
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

    public void setHelmet(org.bukkit.inventory.ItemStack helmet){
        if(helmet!=null){
            customEquipment.put(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(helmet));
            mergeCustomEquipmentPacket();
        }
    }
    public void setChestPlate(org.bukkit.inventory.ItemStack chestPlate){
        if(chestPlate!=null){
            customEquipment.put(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(chestPlate));
            mergeCustomEquipmentPacket();
        }
    }

    public void setLeggings(org.bukkit.inventory.ItemStack leggings){
        if(leggings!=null){
            customEquipment.put(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(leggings));
            mergeCustomEquipmentPacket();
        }
    }

    public void setBoots(org.bukkit.inventory.ItemStack boots){
        if(boots!=null){
            customEquipment.put(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(boots));
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
