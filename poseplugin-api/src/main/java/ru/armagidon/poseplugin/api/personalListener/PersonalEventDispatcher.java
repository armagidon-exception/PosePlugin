package ru.armagidon.poseplugin.api.personalListener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.PlayerArmorChangeEvent;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;

public class PersonalEventDispatcher implements Listener
{

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event){
        PosePluginAPI.getAPI().getPersonalHandlerList().dispatch(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event){
        if(!(event.getEntity() instanceof Player player)) return;
        PosePluginAPI.getAPI().getPersonalHandlerList().dispatch(player, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        PosePluginAPI.getAPI().getPersonalHandlerList().dispatch(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void gameMode(PlayerGameModeChangeEvent event) {
        PosePluginAPI.getAPI().getPersonalHandlerList().dispatch(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event){
        PosePluginAPI.getAPI().getPersonalHandlerList().dispatch(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(PlayerInteractEvent event){
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR)||event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            PosePluginAPI.getAPI().getPersonalHandlerList().dispatch(event.getPlayer(), event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event){
        //If player's not in player list, ignore him
        PosePluginAPI.getAPI().getPersonalHandlerList().dispatch(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void death(PlayerDeathEvent event){
        //If player's not in player list, ignore him
        PosePluginAPI.getAPI().getPersonalHandlerList().dispatch(event.getEntity(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event){
        PosePluginAPI.getAPI().getPersonalHandlerList().dispatch(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public final void onTeleport(PlayerTeleportEvent event){
        PosePluginAPI.getAPI().getPersonalHandlerList().dispatch(event.getPlayer(), event);
    }

    @EventHandler
    public final void onArmorChange(PlayerArmorChangeEvent event){
        PosePluginAPI.getAPI().getPersonalHandlerList().dispatch(event.getPlayer(), event);
    }
}
