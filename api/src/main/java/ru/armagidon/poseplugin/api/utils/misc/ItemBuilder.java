package ru.armagidon.poseplugin.api.utils.misc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class ItemBuilder
{
    private ItemStack item;
    private ItemMeta meta;

    public static ItemBuilder create(Material material){
        return create(new ItemStack(material));
    }

    public static ItemBuilder create(ItemStack stack){
        return new ItemBuilder(stack);
    }

    private ItemBuilder(ItemStack item) {
        this.item = item;
        meta = item.getItemMeta();
    }

    public ItemBuilder setDisplayName(String name){
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return this;
    }
    public ItemBuilder setLore(List<String> s){
        meta.setLore(s);
        item.setItemMeta(meta);
        return this;
    }
    public ItemBuilder setEnchantment(Enchantment enchantment, int level){
        meta.addEnchant(enchantment,level,true);
        item.setItemMeta(meta);
        return this;
    }
    public ItemBuilder setAmount(int amount)
    {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder addStringTag(String key, String value){
        NBTModifier.setString(this.item, key, value);
        return this;
    }

    public ItemBuilder setEnchanted(boolean enchanted){
        if(enchanted){
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.removeEnchant(Enchantment.DURABILITY);
        }
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack asItemStack() {
        return item;
    }

    public ItemBuilder addFlags(ItemFlag... flags){
        meta.addItemFlags(flags);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder skull(UUID uuid){
        item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        meta.setOwningPlayer(p);
        this.meta = meta;
        item.setItemMeta(meta);
        return this;
    }

}
