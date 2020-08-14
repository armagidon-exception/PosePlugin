package ru.armagidon.poseplugin.api.utils.nms.npc;

import org.bukkit.entity.Player;

public interface CustomEquipmentInterface
{
    void showEquipment(Player receiver);

    void setItemInMainHand(org.bukkit.inventory.ItemStack hand);

    void setItemInOffHand(org.bukkit.inventory.ItemStack hand);

    void setHelmet(org.bukkit.inventory.ItemStack helmet);

    void setChestPlate(org.bukkit.inventory.ItemStack chestPlate);

    void setLeggings(org.bukkit.inventory.ItemStack leggings);

    void setBoots(org.bukkit.inventory.ItemStack boots);
}
