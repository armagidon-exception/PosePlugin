package ru.armagidon.poseplugin.plugin.commands.corewrapper;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;

public class PaperCoreWrapper implements CoreWrapper
{

    public PaperCoreWrapper(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public CommandMap getCommandMap() {
        return Bukkit.getCommandMap();
    }

    @Override
    public String getPermissionMessage() {
        return Bukkit.getPermissionMessage();
    }

    @EventHandler
    public void onEvent(PlayerArmorChangeEvent event){
        Bukkit.getPluginManager().callEvent(new ru.armagidon.poseplugin.api.events.
                PlayerArmorChangeEvent(event.getOldItem(), event.getNewItem(),
                ru.armagidon.poseplugin.api.events.PlayerArmorChangeEvent.SlotType.valueOf(event.getSlotType().name()), event.getPlayer()));
    }
}
