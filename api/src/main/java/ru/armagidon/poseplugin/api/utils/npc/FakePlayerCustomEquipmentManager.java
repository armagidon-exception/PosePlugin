package ru.armagidon.poseplugin.api.utils.npc;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public interface FakePlayerCustomEquipmentManager
{
    void showEquipment(Player receiver);

    void setPieceOfEquipment(EquipmentSlot slotType, ItemStack stack);

    void removePieceOfEquipment(EquipmentSlot slot);
}
