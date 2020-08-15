package ru.armagidon.poseplugin.api.utils.nms.item;

import net.minecraft.server.v1_16_R2.NBTBase;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.NBTTagInt;
import net.minecraft.server.v1_16_R2.NBTTagString;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.items.ItemUtil;

public class ItemUtil_v1_16_R2 extends ItemUtil
{

    public ItemUtil_v1_16_R2(ItemStack source) {
        super(source);
    }

    @Override
    public <T> ItemUtil addTag(String name, T value) {

        net.minecraft.server.v1_16_R2.ItemStack stack = CraftItemStack.asNMSCopy(getSource());

        NBTTagCompound compound = stack.getOrCreateTag();

        NBTBase data = null;
        if(value.getClass().getSimpleName().equalsIgnoreCase("integer")){
            data = NBTTagInt.a((Integer) value);
        } else if(value.getClass().getSimpleName().equalsIgnoreCase("String")){
            data = NBTTagString.a((String) value);
        }
        compound.set(name, data);
        stack.setTag(compound);

        setSource(CraftItemStack.asBukkitCopy(stack));
        return this;
    }

    @Override
    public ItemUtil removeTag(String tag) {
        net.minecraft.server.v1_16_R2.ItemStack stack = CraftItemStack.asNMSCopy(getSource());
        if(stack.getTag()!=null) {
            if(stack.getTag().hasKey(tag))
                stack.getTag().remove(tag);
        }
        setSource(CraftItemStack.asBukkitCopy(stack));
        return this;
    }

    @Override
    public boolean contains(String name) {
        net.minecraft.server.v1_16_R2.ItemStack stack = CraftItemStack.asNMSCopy(getSource());
        return stack.getTag()!=null&&stack.getTag().hasKey(name);
    }
}
