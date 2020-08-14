package ru.armagidon.poseplugin.api.utils.nms.npc;

import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityEquipment;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.nms.util.PacketContainer;

import java.util.HashMap;
import java.util.Map;

public class CustomEquipmentInterfaceImpl_v1_15_R1 implements CustomEquipmentInterface
{
    private final FakePlayer_v1_15_R1 npc;
    private PacketContainer<PacketPlayOutEntityEquipment> customEquipmentPacket;
    private final Map<EnumItemSlot, net.minecraft.server.v1_15_R1.ItemStack> customEquipment;

    public CustomEquipmentInterfaceImpl_v1_15_R1(FakePlayer_v1_15_R1 npc) {
        this.npc = npc;
        this.customEquipment = new HashMap<>();
    }

    public void showEquipment(Player receiver){
        if(!customEquipment.isEmpty())
            customEquipmentPacket.send(receiver);
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
        PacketPlayOutEntityEquipment[] eq = customEquipment.entrySet().stream().
                map(entry->new PacketPlayOutEntityEquipment(npc.getFake().getId(),entry.getKey(),entry.getValue()))
                .toArray(PacketPlayOutEntityEquipment[]::new);
        customEquipmentPacket = new PacketContainer<>(eq);
        npc.updateNPC();
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

}
