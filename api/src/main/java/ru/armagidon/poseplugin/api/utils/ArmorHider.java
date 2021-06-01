package ru.armagidon.poseplugin.api.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.misc.NBTModifier;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.PlayerArmorChangeEvent;
import ru.armagidon.poseplugin.api.ticking.Tickable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static ru.armagidon.poseplugin.api.PosePluginAPI.NBT_TAG;

public class ArmorHider implements Listener, Tickable {

    private final String HIDE_VALUE = "ARMOR";

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
            NBTModifier.setString(content, NBT_TAG, HIDE_VALUE);
        }
        player.getInventory().setArmorContents(armor);
        player.updateInventory();
    }

    public void showArmor(Player player){
        if(!hiddenArmor.contains(player)) return;
        hiddenArmor.remove(player);
        ItemStack[] armor = player.getInventory().getArmorContents();
        Arrays.stream(armor).forEach(i -> NBTModifier.remove(i, NBT_TAG));
        player.updateInventory();
    }

    @EventHandler
    public void onArmor(PlayerArmorChangeEvent event){
        if(!hiddenArmor.contains(event.getPlayer())) return;
        ItemStack newItem = event.getNewItem();
        if(newItem == null || newItem.getType().equals(Material.AIR)) return;
        if(event.getPlayer().getEquipment()==null) return;
        addTag(event.getPlayer());
        event.getPlayer().updateInventory();
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event){
        if ( !hiddenArmor.contains(event.getPlayer()) ) return;
        ItemStack item = event.getItemDrop().getItemStack();
        event.getItemDrop().setItemStack(item);
    }

    private void addTag(Player player){
        if(player.getEquipment()==null) return;

        ItemStack[] armor = player.getEquipment().getArmorContents();

        Arrays.stream(armor).filter(item -> item != null && item.getType() != Material.AIR)
                .forEach(item -> NBTModifier.setString(item, NBT_TAG, HIDE_VALUE));

        player.getEquipment().setArmorContents(armor);
    }

    @Override
    public void tick() {
        hiddenArmor.forEach(player -> {
            ItemStack[] storage = player.getInventory().getStorageContents();
            Arrays.stream(storage).filter(item -> item != null && item.getType() != Material.AIR)
                    .forEach(item -> NBTModifier.remove(item, NBT_TAG));
            player.getInventory().setStorageContents(storage);

            NBTModifier.remove(player.getInventory().getItemInMainHand(), NBT_TAG);

            NBTModifier.remove(player.getInventory().getItemInOffHand(), NBT_TAG);
        });
    }
}
