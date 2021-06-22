package ru.armagidon.poseplugin.api.utils.nms.npc;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.misc.DataTable;
import ru.armagidon.poseplugin.api.utils.nms.Showable;
import ru.armagidon.poseplugin.api.utils.nms.Updatable;

import java.util.List;
import java.util.Map;

public abstract class NPCInventory<T> implements Updatable, Showable
{

    @Setter protected ItemMapper itemMapper = ItemMapper.EMPTY;
    protected final FakePlayer<T> fakePlayer;
    @Getter private final DataTable<EquipmentSlot, ItemStack> customEquipment = new DataTable<>();

    public NPCInventory(FakePlayer<T> fakePlayer) {
        this.fakePlayer = fakePlayer;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            customEquipment.define(slot, new ItemStack(Material.AIR));
        }
        mergeCustomEquipmentPacket();
    }

    public final void setPieceOfEquipment(EquipmentSlot slotType, ItemStack hand) {
        if(hand == null) return;
        customEquipment.set(slotType, hand);
        mergeCustomEquipmentPacket();
    }

    public final void setPiecesOfEquipment(List<Map.Entry<EquipmentSlot, ItemStack>> equipment) {
        if (equipment == null || equipment.isEmpty()) return;
        equipment.forEach(entry -> customEquipment.set(entry.getKey(), entry.getValue()));
        mergeCustomEquipmentPacket();
    }

    public final void removePieceOfEquipment(EquipmentSlot slot) {
        if(slot == null) return;

        customEquipment.set(slot, new ItemStack(Material.AIR));

        mergeCustomEquipmentPacket();
    }

    public abstract void mergeCustomEquipmentPacket();
}
