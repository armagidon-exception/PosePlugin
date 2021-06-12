package ru.armagidon.poseplugin.api.utils.nms.npc;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public abstract class NPCInventory<T>
{

    protected final FakePlayer<T> fakePlayer;
    protected final Map<EquipmentSlot, ItemStack> customEquipment = new HashMap<>();

    public NPCInventory(FakePlayer<T> fakePlayer) {
        this.fakePlayer = fakePlayer;
    }

    public abstract void showEquipment(Player receiver);

    public abstract void setPieceOfEquipment(EquipmentSlot slotType, ItemStack stack);

    public abstract void removePieceOfEquipment(EquipmentSlot slot);
}
