package ru.armagidon.poseplugin.plugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffectType;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.StopPosingEvent;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.utils.misc.BlockPositionUtils;
import ru.armagidon.poseplugin.plugin.events.StopAnimationWithMessageEvent;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.armagidon.poseplugin.PosePlugin.PLAYERS_POSES;
import static ru.armagidon.poseplugin.api.poses.EnumPose.*;

public class PluginEventListener implements Listener
{

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        if(PosePlugin.checker != null){
            if(!PosePlugin.checker.uptodate && event.getPlayer().hasPermission("poseplugin.admin")){
                PosePlugin.checker.sendNotification(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPotionEffectObtain(EntityPotionEffectEvent event){
        if(!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getEntity();
        if(!PosePluginAPI.getAPI().getPlayerMap().containsPlayer(player)) return;
        PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(player.getName());
        if(!p.getPoseType().equals(EnumPose.LYING)) return;
        boolean preventInvisible = PosePlugin.getInstance().getCfg().getBoolean("lay.prevent-use-when-invisible");
        if( !preventInvisible ){
            if(event.getAction().equals(EntityPotionEffectEvent.Action.ADDED)){
                if(event.getNewEffect() !=null && event.getNewEffect().getType().equals(PotionEffectType.INVISIBILITY))
                    p.getPose().getProperty(EnumPoseOption.INVISIBLE).setValue(true);
            } else if(event.getAction().equals(EntityPotionEffectEvent.Action.REMOVED)||event.getAction().equals(EntityPotionEffectEvent.Action.CLEARED)){
                if(event.getOldEffect() != null && event.getOldEffect().getType().equals(PotionEffectType.INVISIBILITY))
                    p.getPose().getProperty(EnumPoseOption.INVISIBLE).setValue(false);
            }
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event){
        //Call stop event
        if ( !PLAYERS_POSES.containsKey(event.getPlayer()) ) return;
        PosePluginPlayer player = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(event.getPlayer().getName());

        Set<EnumPose> posesToCheck = Stream.of(WAVING, POINTING, HANDSHAKING).collect(Collectors.toSet());


        if (player.getPoseType().equals(CRAWLING) || player.getPoseType().equals(PRAYING)) {
            player.resetCurrentPose();
        } else if(posesToCheck.contains(player.getPoseType())) {
            if ( PosePlugin.getInstance().getCfg().getBoolean(player.getPoseType().getName()+".disable-when-shift") ) {
                player.resetCurrentPose();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event){
        if(!(event.getEntity() instanceof Player)) return;

        if ( !PLAYERS_POSES.containsKey(event.getEntity()) ) return;

        PosePluginPlayer player = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(event.getEntity().getName());
        if(player.getPoseType() == EnumPose.STANDING) return;
        boolean standUpWhenDamaged = PosePlugin.getInstance().getCfg().getBoolean(player.getPoseType().getName() + ".stand-up-when-damaged");
        if (standUpWhenDamaged) {
            Bukkit.getPluginManager().callEvent(new StopAnimationWithMessageEvent(StopAnimationWithMessageEvent.StopCause.DAMAGE, player, player.getPoseType()));
            PLAYERS_POSES.remove(player.getHandle());
            player.resetCurrentPose();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {

        if( !PLAYERS_POSES.containsKey(event.getPlayer()) ) return;
        PosePluginPlayer player = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(event.getPlayer().getName());
        if(player.getPoseType().equals(EnumPose.STANDING)) return;
        if(event.getNewGameMode().equals(GameMode.SPECTATOR))
            player.resetCurrentPose();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event){
 
        if ( !PLAYERS_POSES.containsKey(event.getPlayer()) ) return;

        if(event.getFrom().getWorld().equals(event.getTo().getWorld()))
            if(event.getFrom().clone().add(.5,0,.5).distance(event.getTo().clone().add(.5,0,.5)) < 1) return;

        if(!PosePluginAPI.getAPI().getPlayerMap().containsPlayer(event.getPlayer())) return;

        PosePluginPlayer player = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(event.getPlayer());

        if(player.getPoseType().equals(EnumPose.STANDING)) return;

        if(event.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN)) return;

        player.resetCurrentPose();

    }

    /*@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public final void onBlockBreak(BlockBreakEvent event) {

        BlockPositionUtils.getNear(5,event.getPlayer()).forEach(near->{
            PosePluginPlayer player = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(event.getPlayer().getName());
            if(player.getPoseType().equals(EnumPose.STANDING)) return;
            if (!BlockPositionUtils.getBelow(player.getHandle().getLocation()).getType().isAir()) {
                player.resetCurrentPose();
            }
        });
    }*/

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public final void onMove(PlayerMoveEvent event){
        if ( !PLAYERS_POSES.containsKey(event.getPlayer())) return;
        PosePluginPlayer player = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(event.getPlayer());
        if (player.getPoseType() != CRAWLING) return;

        boolean isSwimmingStatic = PosePlugin.getInstance().getCfg().getBoolean("swim.static");
        if (isSwimmingStatic){
            if (event.getTo().getX() != event.getFrom().getX() ||
                event.getTo().getY() != event.getFrom().getY() ||
                event.getTo().getZ() != event.getFrom().getZ())

                event.setTo(event.getFrom());
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStop(StopPosingEvent event){
        if ( !PLAYERS_POSES.containsKey(event.getPlayer().getHandle()) ) return;

        Bukkit.getPluginManager().callEvent(new StopAnimationWithMessageEvent(StopAnimationWithMessageEvent.StopCause.OTHER, event.getPlayer(), event.getPose()));
        PLAYERS_POSES.remove(event.getPlayer().getHandle());

    }

}
