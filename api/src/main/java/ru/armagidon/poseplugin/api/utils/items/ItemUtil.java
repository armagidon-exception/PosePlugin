package ru.armagidon.poseplugin.api.utils.items;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

public abstract class ItemUtil {
    private @Getter @Setter ItemStack source;

    public ItemUtil(ItemStack source) {
        this.source = source;
    }
    public abstract  <T> void addTag(String name, T value);

    public abstract boolean contains(String name);

}
