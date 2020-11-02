package ru.armagidon.poseplugin.api.utils.npc.v1_15_R1;

import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityEquipment;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import ru.armagidon.poseplugin.api.utils.nms.util.PacketContainer;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayerCustomEquipmentManager;

import java.util.HashMap;
import java.util.Map;

public class CustomEquipmentManagerImpl implements FakePlayerCustomEquipmentManager
{
    private final FakePlayer npc;
    private PacketContainer<PacketPlayOutEntityEquipment> customEquipmentPacket;
    private final Map<EnumItemSlot, ItemStack> customEquipment;

    public CustomEquipmentManagerImpl(FakePlayer npc) {
        this.npc = npc;
        this.customEquipment = new HashMap<>();
    }

    public void showEquipment(Player receiver){
        if(!customEquipment.isEmpty())
            customEquipmentPacket.send(receiver);
    }

    @Override
    public void setPieceOfEquipment(EquipmentSlot slotType, org.bukkit.inventory.ItemStack hand) {
        if(hand==null) return;
        ItemStack vanillaStack = CraftItemStack.asNMSCopy(hand);
        customEquipment.put(adaptToItemSlot(slotType), vanillaStack);
        mergeCustomEquipmentPacket();
    }

    @Override
    public void removePieceOfEquipment(EquipmentSlot slot) {
        if(slot==null) return;
        EnumItemSlot itemSlot = adaptToItemSlot(slot);
        if(!customEquipment.containsKey(itemSlot)) return;
        customEquipment.remove(itemSlot);
        mergeCustomEquipmentPacket();
    }

    public EnumItemSlot adaptToItemSlot(EquipmentSlot slotType){
        switch (slotType){
            case HAND:
                return EnumItemSlot.MAINHAND;
            case OFF_HAND:
                return EnumItemSlot.OFFHAND;
            case FEET:
                return EnumItemSlot.FEET;
            case LEGS:
                return EnumItemSlot.LEGS;
            case CHEST:
                return EnumItemSlot.CHEST;
            case HEAD:
                return EnumItemSlot.HEAD;
            default:
                throw new IllegalStateException("Unexpected value: " + slotType);
        }
    }

    private void mergeCustomEquipmentPacket() {
        PacketPlayOutEntityEquipment[] eq = customEquipment.entrySet().stream().
                map(entry->new PacketPlayOutEntityEquipment(npc.getFake().getId(),entry.getKey(),entry.getValue()))
                .toArray(PacketPlayOutEntityEquipment[]::new);
        customEquipmentPacket = new PacketContainer<>(eq);
        npc.updateNPC();
    }
}
