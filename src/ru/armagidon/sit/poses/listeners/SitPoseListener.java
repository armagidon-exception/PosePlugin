package ru.armagidon.sit.poses.listeners;

import de.Kurfat.Java.Minecraft.BetterChair.Types.Chair;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.entity.EntityDismountEvent;
import ru.armagidon.sit.SitPlugin;
import ru.armagidon.sit.SitPluginPlayer;
import ru.armagidon.sit.poses.EnumPose;

import java.util.Map;

import static ru.armagidon.sit.utils.Listener.containsPlayer;

public class SitPoseListener implements Listener
{
    private final Map<String, SitPluginPlayer> players = ru.armagidon.sit.utils.Listener.players;

    public SitPoseListener() {
        SitPlugin.getInstance().getServer().getPluginManager().registerEvents(this,SitPlugin.getInstance());
    }

    @EventHandler
    public void onArmor(PlayerArmorStandManipulateEvent event){
        players.values().forEach(p->p.getPose().armor(event));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        SitPluginPlayer p = players.get(event.getPlayer().getName());
        if(p.getPose().getPose().equals(EnumPose.SITTING)) {
            if (event.getFrom().getYaw() != event.getTo().getYaw()) {
                p.getPose().move(event);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if(!containsPlayer(event.getEntity())) return;
        SitPluginPlayer p = players.get(event.getEntity().getName());
        if (p.getPose().getPose().equals(EnumPose.SITTING)) {
            p.getPose().stop(false);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        if(!containsPlayer(event.getPlayer())) return;
        SitPluginPlayer p = players.get(event.getPlayer().getName());
        if(p.getPose().getPose().equals(EnumPose.SITTING)) p.getPose().stop(false);
    }

    @EventHandler
    public void onDismout(EntityDismountEvent event){
        if(event.getEntity() instanceof Player){

            Player player = (Player) event.getEntity();
            if(!containsPlayer(player)) return;
            SitPluginPlayer p = players.get(player.getName());
            if(p.getPose().getPose().equals(EnumPose.SITTING)){
                boolean e = SitPlugin.chairenabled;
                if(e){
                    if(Chair.CACHE_BY_PLAYER.containsKey(player)){
                        return;
                    } else {
                        p.getPose().stop(true);
                    }
                } else p.getPose().stop(true);
            }

        }
    }
}
