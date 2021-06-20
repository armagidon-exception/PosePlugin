package ru.armagidon.poseplugin.plugin.corewrapper;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.PlayerArmorChangeEvent;
import ru.armagidon.poseplugin.api.ticking.Tickable;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpigotCoreWrapper implements CoreWrapper, Tickable
{
    private final Map<Player, ItemStack[]> armors = new ConcurrentHashMap<>();

    public SpigotCoreWrapper(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        PosePluginAPI.getAPI().getTickManager().registerTickModule(this,false);
        Bukkit.getOnlinePlayers().forEach(p->
                armors.computeIfAbsent(p, pl->pl.getInventory().getArmorContents()));
    }

    @SneakyThrows
    @Override
    public CommandMap getCommandMap() {
        Field mapF = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        mapF.setAccessible(true);
        return (CommandMap) mapF.get(Bukkit.getServer());
    }

    @Override
    public String getPermissionMessage() {
        return ChatColor.RED+"I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.";
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event){
        armors.put(event.getPlayer(), event.getPlayer().getInventory().getArmorContents());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        armors.remove(event.getPlayer());
    }

    @Override
    public void tick() {
        armors.forEach((player, armor) -> {
            EntityEquipment equipment = player.getEquipment();
            if(equipment==null) return;
            checkArmor(player, equipment.getBoots(), armor, 0);
            checkArmor(player, equipment.getLeggings(), armor, 1);
            checkArmor(player, equipment.getChestplate(), armor, 2);
            checkArmor(player, equipment.getHelmet(), armor, 3);
        });
    }

    private PlayerArmorChangeEvent.SlotType getSlot(int i){
        switch (i){
            case 0:
                return PlayerArmorChangeEvent.SlotType.FEET;
            case 1:
                return PlayerArmorChangeEvent.SlotType.LEGS;
            case 2:
                return PlayerArmorChangeEvent.SlotType.CHEST;
            case 3:
                return PlayerArmorChangeEvent.SlotType.HEAD;
            default:
                return null;
        }
    }

    private void checkArmor(Player player,ItemStack piece, ItemStack[] armor, int index){
        if(piece==null) piece = new ItemStack(Material.AIR);
        if(!piece.isSimilar(armor[index])){
            Bukkit.getPluginManager().callEvent(new PlayerArmorChangeEvent(armor[index],piece,getSlot(index),player));
            armor[index] = piece;
            armors.put(player, armor);
        }
    }
}
