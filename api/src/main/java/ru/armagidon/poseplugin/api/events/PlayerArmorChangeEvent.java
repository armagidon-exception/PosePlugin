package ru.armagidon.poseplugin.api.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PlayerArmorChangeEvent extends Event
{

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private @Getter final ItemStack oldItem;
    private @Getter final ItemStack newItem;
    private @Getter final SlotType slotType;
    private @Getter final Player player;

    public PlayerArmorChangeEvent(ItemStack oldItem, ItemStack newItem, SlotType slot, Player player) {
        this.oldItem = oldItem;
        this.newItem = newItem;
        this.slotType = slot;
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public enum SlotType{
        HEAD, CHEST, LEGS, FEET;
    }
}
