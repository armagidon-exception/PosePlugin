package ru.armagidon.poseplugin.api.utils.nms.npc;

import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.nms.Updatable;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class NPCInventory<T> implements Updatable
{

    @Setter protected ItemMapper itemMapper = ItemMapper.EMPTY;
    protected final FakePlayer<T> fakePlayer;
    protected final Map<EquipmentSlot, ItemStack> customEquipment = new HashMap<>();

    public NPCInventory(FakePlayer<T> fakePlayer) {
        this.fakePlayer = fakePlayer;
    }

    public abstract void showEquipment(Player receiver);

    public final void setPieceOfEquipment(EquipmentSlot slotType, org.bukkit.inventory.ItemStack hand) {
        if(hand == null) return;
        customEquipment.put(slotType, itemMapper.map(hand));
        mergeCustomEquipmentPacket();
    }

    public final void setPiecesOfEquipment(List<Map.Entry<EquipmentSlot, org.bukkit.inventory.ItemStack>> equipment) {
        if (equipment == null || equipment.isEmpty()) return;
        equipment.forEach(entry -> customEquipment.put(entry.getKey(), itemMapper.map(entry.getValue())));
        mergeCustomEquipmentPacket();
    }

    public final void removePieceOfEquipment(EquipmentSlot slot) {
        if(slot == null) return;
        if(!customEquipment.containsKey(slot)) return;
        customEquipment.remove(slot);
        mergeCustomEquipmentPacket();
    }

    public abstract void mergeCustomEquipmentPacket();

    @Override
    public final void update() {
        fakePlayer.getTrackers().forEach(this::showEquipment);
    }
}
