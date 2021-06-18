package ru.armagidon.poseplugin.api.poses.experimental;

import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.misc.NBTModifier;
import ru.armagidon.poseplugin.api.utils.nms.npc.ItemMapper;

public class TagRemovingMapper implements ItemMapper
{

    private final String tag;

    public TagRemovingMapper(String tag) {
        this.tag = tag;
    }

    @Override
    public ItemStack map(ItemStack input) {
        NBTModifier.remove(input, tag);
        return input;
    }
}
