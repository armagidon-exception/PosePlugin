package ru.armagidon.poseplugin.api.utils.nms.npc;

import org.bukkit.inventory.ItemStack;

public interface ItemMapper
{
    ItemMapper EMPTY = input -> input;

    ItemStack map(ItemStack input);
}
