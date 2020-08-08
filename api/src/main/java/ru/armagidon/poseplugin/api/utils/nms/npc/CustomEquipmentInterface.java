package ru.armagidon.poseplugin.api.utils.nms.npc;

import org.bukkit.entity.Player;

public interface CustomEquipmentInterface
{
    void showEquipment(Player receiver);

    void setItemInMainHand(org.bukkit.inventory.ItemStack hand);

    void setItemInOffHand(org.bukkit.inventory.ItemStack hand);
}
