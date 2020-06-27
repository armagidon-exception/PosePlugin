package ru.armagidon.poseplugin.api.personalListener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.PosePluginPlayer;

public class PersonalEventDispatcher implements Listener
{
    @EventHandler(ignoreCancelled = true,priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        PosePluginPlayer player = PosePlugin.getInstance().getPosePluginPlayer(event.getPlayer().getName());
        player.callPersonalEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent e){
        if(!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if(!containsPlayer(p)) return;
        PosePluginPlayer player = PosePlugin.getInstance().getPosePluginPlayer(p.getName());
        player.callPersonalEvent(e);
    }

    private boolean containsPlayer(Player player) {
        return PosePlugin.getInstance().containsPlayer(player);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!containsPlayer(event.getPlayer())) return;
        PosePluginPlayer p = PosePlugin.getInstance().getPosePluginPlayer(event.getPlayer().getName());
        p.callPersonalEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (!containsPlayer(event.getPlayer())) return;
        PosePluginPlayer p = PosePlugin.getInstance().getPosePluginPlayer(event.getPlayer().getName());
        p.callPersonalEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void gameMode(PlayerGameModeChangeEvent event) {
        if (!containsPlayer(event.getPlayer())) return;
        PosePluginPlayer p = PosePlugin.getInstance().getPosePluginPlayer(event.getPlayer().getName());
        p.callPersonalEvent(event);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event){
        if(!PosePlugin.getInstance().containsPlayer(event.getPlayer())) return;
        PosePluginPlayer player = PosePlugin.getInstance().getPosePluginPlayer(event.getPlayer().getName());
        player.callPersonalEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(PlayerInteractEvent event){
        if(!PosePlugin.getInstance().containsPlayer(event.getPlayer())) return;
        PosePluginPlayer player = PosePlugin.getInstance().getPosePluginPlayer(event.getPlayer().getName());
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR)||event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            player.callPersonalEvent(event);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onGameModeChange(PlayerGameModeChangeEvent event){
        if(!PosePlugin.getInstance().containsPlayer(event.getPlayer())) return;
        PosePluginPlayer player = PosePlugin.getInstance().getPosePluginPlayer(event.getPlayer().getName());
        player.callPersonalEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSneak(PlayerToggleSneakEvent event){
        //If player's not in player list, ignore him
        if(!containsPlayer(event.getPlayer())) return;
        PosePluginPlayer p = PosePlugin.getInstance().getPosePluginPlayer(event.getPlayer().getName());
        p.callPersonalEvent(event);
    }

}
