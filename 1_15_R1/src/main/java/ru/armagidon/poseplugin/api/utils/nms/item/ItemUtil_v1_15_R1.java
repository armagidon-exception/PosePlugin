package ru.armagidon.poseplugin.api.utils.nms.item;

import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.items.ItemUtil;

public class ItemUtil_v1_15_R1 extends ItemUtil {

    public ItemUtil_v1_15_R1(ItemStack source) {
        super(source);
    }

    @Override
    public <T> void addTag(String name, T value) {

    }

    @Override
    public boolean contains(String name) {
        return false;
    }
}
