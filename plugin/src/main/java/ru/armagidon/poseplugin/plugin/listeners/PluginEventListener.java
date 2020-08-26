package ru.armagidon.poseplugin.plugin.listeners;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffectType;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.PostPoseChangeEvent;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.IPluginPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.api.utils.property.PropertyMap;
import ru.armagidon.poseplugin.plugin.configuration.ConfigConstants;

public class PluginEventListener implements Listener
{

    @EventHandler
    public void onEvent(PlayerJoinEvent event){
        if(PosePlugin.checker !=null){
            if(!PosePlugin.checker.uptodate&&event.getPlayer().hasPermission("poseplugin.admin")){
                PosePlugin.checker.sendNotification(event.getPlayer());
            }
        }
    }


    @EventHandler
    public void onEvent(PostPoseChangeEvent event){
        String sec = event.getPose().getName();
        ConfigurationSection config = PosePlugin.getInstance().getConfig().getConfigurationSection(sec);
        if(config==null) return;
        IPluginPose pose = event.getPlayer().getPose();
        PropertyMap properties = pose.getProperties();
        if(event.getPlayer().getPose().isAPIModeActivated()) return;
        switch (event.getPose()){
            case LYING: {
                properties.getProperty("head-rotation", Boolean.class).initialize(config.getBoolean("head-rotation"));
                properties.getProperty("swing-animation", Boolean.class).initialize(config.getBoolean("swing-animation"));
                properties.getProperty("update-equipment", Boolean.class).initialize(config.getBoolean("update-equipment"));
                properties.getProperty("update-overlays", Boolean.class).initialize(config.getBoolean("update-overlays"));
                properties.getProperty("view-distance", Integer.class).initialize(config.getInt("view-distance"));
                boolean preventInvisible = config.getBoolean("lay.prevent-use-when-invisible");
                if(!preventInvisible&&event.getPlayer().getHandle().hasPotionEffect(PotionEffectType.INVISIBILITY)){
                    properties.getProperty("invisible",Boolean.class).initialize(true);
                }
                break;
            }
            case SWIMMING:
                if(PosePlugin.getInstance().getConfig().getBoolean(sec+".static")){
                    properties.getProperty("static", Boolean.class).initialize(true);
                }
        }
    }

    @EventHandler
    public void onEvent(EntityPotionEffectEvent event){
        if(!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getEntity();
        if(!PosePluginAPI.getAPI().getPlayerMap().containsPlayer(player)) return;
        PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(player.getName());
        if(!p.getPoseType().equals(EnumPose.LYING)) return;
        boolean preventInvisible = PosePlugin.getInstance().getConfig().getBoolean("lay.prevent-use-when-invisible");
        PropertyMap properties = p.getPose().getProperties();
        if(p.getPose().isAPIModeActivated()) return;
        if(!preventInvisible){
            if(event.getAction().equals(EntityPotionEffectEvent.Action.ADDED)){
                if(event.getNewEffect()!=null&&event.getNewEffect().getType().equals(PotionEffectType.INVISIBILITY)) {
                    properties.getProperty("invisible", Boolean.class).initialize(true);
                }
            } else if(event.getAction().equals(EntityPotionEffectEvent.Action.REMOVED)||event.getAction().equals(EntityPotionEffectEvent.Action.CLEARED)){
                if(event.getOldEffect()!=null&&event.getOldEffect().getType().equals(PotionEffectType.INVISIBILITY))
                properties.getProperty("invisible",Boolean.class).initialize(false);
            }
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event){
        //Call stop event
        if(!PosePluginAPI.getAPI().getPlayerMap().containsPlayer(event.getPlayer())) return;
        PosePluginPlayer player = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(event.getPlayer().getName());
        if(player.getPose().isAPIModeActivated()) return;
        switch (player.getPoseType()){
            case SWIMMING:{
                if (event.getPlayer().isOnGround()) {
                    PluginPose.callStopEvent(player.getPoseType(), player, StopAnimationEvent.StopCause.STOPPED);
                }
                break;
            }
            case WAVING:{
                if(ConfigConstants.isWaveShiftEnabled()){
                    PluginPose.callStopEvent(player.getPoseType(), player, StopAnimationEvent.StopCause.STOPPED);
                }
                break;
            }
            case POINTING:{
                if(ConfigConstants.isPointShiftEnabled()){
                    PluginPose.callStopEvent(player.getPoseType(), player, StopAnimationEvent.StopCause.STOPPED);
                }
                break;
            }
            case HANDSHAKING:{
                if(ConfigConstants.isHandShakeShiftEnabled()){
                    PluginPose.callStopEvent(player.getPoseType(), player, StopAnimationEvent.StopCause.STOPPED);
                }
                break;
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event){
        if(event.isCancelled()) return;
        if(!(event.getEntity() instanceof Player)) return;
        if(!PosePluginAPI.getAPI().getPlayerMap().containsPlayer((Player) event.getEntity())) return;
        PosePluginPlayer player = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(event.getEntity().getName());
        if(player.getPoseType().equals(EnumPose.STANDING)) return;
        if(player.getPose().isAPIModeActivated()) return;
        boolean standUpWhenDamaged = PosePlugin.getInstance().getConfig().getBoolean(player.getPoseType().getName()+".stand-up-when-damaged");
        if(standUpWhenDamaged)
            PluginPose.callStopEvent(player.getPoseType(),player, StopAnimationEvent.StopCause.STOPPED, "DAMAGE");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGameModeChange(PlayerGameModeChangeEvent event){
        if(event.isCancelled()) return;
        if(!PosePluginAPI.getAPI().getPlayerMap().containsPlayer(event.getPlayer())) return;
        PosePluginPlayer player = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(event.getPlayer().getName());
        if(player.getPoseType().equals(EnumPose.STANDING)) return;
        if(player.getPose().isAPIModeActivated()) return;
        if(event.getNewGameMode().equals(GameMode.SPECTATOR))
            PluginPose.callStopEvent(player.getPoseType(),player, StopAnimationEvent.StopCause.STOPPED, "GAMEMODE");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event){
        if(event.isCancelled()) return;
        if(event.getFrom().getWorld().equals(event.getTo().getWorld()))
            if(event.getFrom().clone().add(.5,0,.5).distance(event.getTo().clone().add(.5,0,.5))<1) return;
        if(!PosePluginAPI.getAPI().getPlayerMap().containsPlayer(event.getPlayer())) return;
        PosePluginPlayer player = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(event.getPlayer().getName());
        if(player.getPoseType().equals(EnumPose.STANDING)) return;
        if(player.getPose().isAPIModeActivated()) return;
        if(event.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN)) return;
        PluginPose.callStopEvent(player.getPoseType(),player, StopAnimationEvent.StopCause.STOPPED, "TELEPORT");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public final void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        VectorUtils.getNear(5,event.getPlayer()).forEach(near->{
            Block under = VectorUtils.getBlockOnLoc(near.getLocation()).getRelative(BlockFace.DOWN);
            PosePluginPlayer player = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(event.getPlayer().getName());
            if(player.getPoseType().equals(EnumPose.STANDING)) return;
            if(player.getPose().isAPIModeActivated()) return;
            if (event.getBlock().equals(under)) {
                PluginPose.callStopEvent(player.getPoseType(), player, StopAnimationEvent.StopCause.STOPPED, "BLOCKUPDATE");
            }
        });
    }
}
