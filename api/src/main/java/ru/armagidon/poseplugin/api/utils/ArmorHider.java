package ru.armagidon.poseplugin.api.utils;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.armagidonapi.itemutils.ItemModifingPipeline;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.PlayerArmorChangeEvent;
import ru.armagidon.poseplugin.api.ticking.Tickable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static ru.armagidon.poseplugin.api.PosePluginAPI.pluginTagClear;

public class ArmorHider implements Listener, Tickable {

    private final String HIDE_KEY = "PosePluginItem";
    private final String HIDE_VALUE = "ARMOR";

    private final ItemModifingPipeline pluginAddTag = new ItemModifingPipeline(){{
        addLast(i -> addHideTag(i));
    }};

    private final Set<Player> hiddenArmor;

    public ArmorHider() {
        PosePluginAPI.getAPI().registerListener(this);
        hiddenArmor = new HashSet<>();
        PosePluginAPI.getAPI().getTickManager().registerTickModule(this,false);
    }

    public void hideArmor(Player player){
        if(hiddenArmor.contains(player)) return;
        hiddenArmor.add(player);
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack content : armor) {
            if(content == null || content.getType() == Material.AIR) continue;
            pluginAddTag.pushThrough(content);
        }
        player.getInventory().setArmorContents(armor);
        player.updateInventory();
    }

    public void showArmor(Player player){
        if(!hiddenArmor.contains(player)) return;
        hiddenArmor.remove(player);
        ItemStack[] armor = player.getInventory().getArmorContents();
        Arrays.stream(armor).forEach(pluginTagClear::pushThrough);
        player.getInventory().setArmorContents(armor);
        player.updateInventory();
    }

    private ItemStack addHideTag(ItemStack stack){
        NBTItem item = new NBTItem(stack, true);
        item.setString(HIDE_KEY,HIDE_VALUE);
        return item.getItem();
    }

    @EventHandler
    public void onArmor(PlayerArmorChangeEvent event){
        if(!hiddenArmor.contains(event.getPlayer())) return;
        ItemStack newItem = event.getNewItem();
        if(newItem==null || newItem.getType().equals(Material.AIR)) return;
        if(event.getPlayer().getEquipment()==null) return;
        addTag(event.getPlayer());
        event.getPlayer().updateInventory();
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event){
        if ( !hiddenArmor.contains(event.getPlayer()) ) return;
        ItemStack item = event.getItemDrop().getItemStack();
        pluginTagClear.pushThrough(item);
        event.getItemDrop().setItemStack(item);
    }

    private void addTag(Player player){
        if(player.getEquipment()==null) return;

        ItemStack[] armor = player.getEquipment().getArmorContents();
        for (ItemStack content : armor) {
            if(content == null || content.getType() == Material.AIR) continue;
            pluginAddTag.pushThrough(content);
        }
        player.getEquipment().setArmorContents(armor);
    }

    @Override
    public void tick() {
        hiddenArmor.forEach(player -> {
            ItemStack[] armor = player.getInventory().getStorageContents();
            for (ItemStack content : armor) {
                if(content == null || content.getType() == Material.AIR) continue;
                pluginTagClear.pushThrough(content);
            }
            player.getInventory().setStorageContents(armor);

            ItemStack main = player.getInventory().getItemInMainHand();
            pluginTagClear.pushThrough(main);
            player.getInventory().setItemInMainHand(main);

            ItemStack off = player.getInventory().getItemInOffHand();
            pluginTagClear.pushThrough(off);
            player.getInventory().setItemInOffHand(off);
        });
    }
}
