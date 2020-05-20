package ru.armagidon.poseplugin.api.poses.personalListener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginPlayer;

public class PersonalEventDispatcher implements Listener
{
    @EventHandler(ignoreCancelled = true,priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        PosePluginPlayer player = PosePlugin.getInstance().getPosePluginPlayer(event.getPlayer().getName());
        player.callPersonalEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!containsPlayer(event.getPlayer())) return;
        if (!event.getPlayer().getName().equalsIgnoreCase(event.getPlayer().getName())) return;
        PosePluginPlayer p = PosePlugin.getInstance().getPosePluginPlayer(event.getPlayer().getName());
        p.callPersonalEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!containsPlayer(event.getPlayer())) return;
        if (!event.getPlayer().getName().equalsIgnoreCase(event.getPlayer().getName())) return;
        PosePluginPlayer p = PosePlugin.getInstance().getPosePluginPlayer(event.getPlayer().getName());
        p.callPersonalEvent(event);
    }
}
