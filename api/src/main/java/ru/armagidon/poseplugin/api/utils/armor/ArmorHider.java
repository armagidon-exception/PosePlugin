package ru.armagidon.poseplugin.api.utils.armor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.PlayerArmorChangeEvent;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.items.ItemUtil;

import java.util.HashSet;
import java.util.Set;

public class ArmorHider implements Listener, Tickable {

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
        for (int i = 0; i < armor.length; i++) {
            armor[i] = addHideTag(armor[i]);
        }
        player.getInventory().setArmorContents(armor);
        player.updateInventory();
    }

    public void showArmor(Player player){
        if(!hiddenArmor.contains(player)) return;
        hiddenArmor.remove(player);
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            armor[i] = removeHideTag(armor[i]);
        }
        player.getInventory().setArmorContents(armor);
        player.updateInventory();
    }

    private ItemStack addHideTag(ItemStack stack){
        return PosePluginAPI.getAPI().getNMSFactory().createItemUtil(stack).addTag("PosePluginItem","ARMOR").getSource();
    }

    private ItemStack removeHideTag(ItemStack stack){
        return PosePluginAPI.getAPI().getNMSFactory().createItemUtil(stack).removeTag("PosePluginItem").getSource();
    }

    @EventHandler
    public void onArmor(PlayerArmorChangeEvent event){
        if(!hiddenArmor.contains(event.getPlayer())) return;
        ItemStack newItem = event.getNewItem();
        if(newItem==null||newItem.getType().equals(Material.AIR)) return;
        if(event.getPlayer().getEquipment()==null) return;
        addTag(event.getPlayer(), newItem,event.getSlotType());
        event.getPlayer().updateInventory();
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event){
        ItemStack item = event.getItemDrop().getItemStack();
        ItemUtil itemUtil = PosePluginAPI.getAPI().getNMSFactory().createItemUtil(item);
        if(itemUtil.contains("PosePluginItem")){
            event.getItemDrop().setItemStack(itemUtil.removeTag("PosePluginItem").getSource());
        }
    }

    private void addTag(Player player, ItemStack input, PlayerArmorChangeEvent.SlotType slot){
        if(player.getEquipment()==null) return;
        switch (slot){
            case FEET:
                player.getEquipment().setBoots(addHideTag(input));
                break;
            case LEGS:
                player.getEquipment().setLeggings(addHideTag(input));
                break;
            case CHEST:
                player.getEquipment().setChestplate(addHideTag(input));
                break;
            case HEAD:
                player.getEquipment().setHelmet(addHideTag(input));
                break;
        }
    }

    @Override
    public void tick() {
        hiddenArmor.forEach(player -> {
            ItemStack[] contents = player.getInventory().getStorageContents();
            ItemUtil util = PosePluginAPI.getAPI().getNMSFactory().createItemUtil(new ItemStack(Material.AIR));
            for (int i = 0; i < contents.length; i++) {
                util.setSource(contents[i]);
                if(util.contains("PosePluginItem")){
                    contents[i]=util.removeTag("PosePluginItem").getSource();
                }
            }
            player.getInventory().setStorageContents(contents);
            util.setSource(player.getInventory().getItemInMainHand());
            if(util.contains("PosePluginItem")){
                player.getInventory().setItemInMainHand(util.removeTag("PosePluginItem").getSource());
            }
            util.setSource(player.getInventory().getItemInOffHand());
            if(util.contains("PosePluginItem")){
                player.getInventory().setItemInOffHand(util.removeTag("PosePluginItem").getSource());
            }
        });
    }
}
