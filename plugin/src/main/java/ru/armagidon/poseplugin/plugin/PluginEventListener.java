package ru.armagidon.poseplugin.plugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.PoseChangeEvent;
import ru.armagidon.poseplugin.api.events.PostPoseChangeEvent;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.IPluginPose;
import ru.armagidon.poseplugin.api.utils.property.PropertyMap;
import ru.armagidon.poseplugin.plugin.messaging.Message;

public class PluginEventListener implements Listener
{

    public PluginEventListener() {
    }

    @EventHandler
    public void onEvent(PlayerJoinEvent event){
        if(PosePlugin.checker !=null){
            if(!PosePlugin.checker.uptodate&&event.getPlayer().hasPermission("poseplugin.admin")){
                PosePlugin.checker.sendNotification(event.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void preventInvisibility(PoseChangeEvent event){
        if(event.isCancelled()) return;
        if(event.getAfter().equals(EnumPose.LYING)){
            Player player = event.getPlayer().getHandle();
            boolean prevent_invisible = PosePlugin.getInstance().getConfig().getBoolean("lay.prevent-use-when-invisible");
            if(prevent_invisible&&player.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                PosePlugin.getInstance().message().send(Message.LAY_PREVENT_INVISIBILITY, player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnimationStart(PoseChangeEvent event){
        if(event.isCancelled()) return;
        String sec = getSection(event.getAfter());
        PosePlugin.getInstance().message().send(sec+".play",event.getPlayer().getHandle());
    }

    @EventHandler
    public void onEvent(PostPoseChangeEvent event){
        String sec = getSection(event.getPose());
        ConfigurationSection config = PosePlugin.getInstance().getConfig().getConfigurationSection(sec);
        if(config==null) return;
        IPluginPose pose = event.getPlayer().getPose();
        PropertyMap properties = pose.getProperties();
        switch (event.getPose()){
            case LYING:
                properties.getProperty("head-rotation", Boolean.class).setValue(config.getBoolean("head-rotation"));
                properties.getProperty("swing-animation", Boolean.class).setValue(config.getBoolean("swing-animation"));
                properties.getProperty("update-equipment", Boolean.class).setValue(config.getBoolean("update-equipment"));
                properties.getProperty("update-overlays", Boolean.class).setValue(config.getBoolean("update-overlays"));
                properties.getProperty("view-distance",Integer.class).setValue(config.getInt("view-distance"));
                break;
            case SWIMMING:
                if(PosePlugin.getInstance().getConfig().getBoolean(sec+".static")){
                    properties.getProperty("static", Boolean.class).setValue(true);
                }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStop(StopAnimationEvent e){
        PosePluginPlayer p = e.getPlayer();
        switch (e.getCause()){
            case DAMAGE:{
                String sec = getSection(p.getPoseType());
                boolean standupwhendamage = PosePlugin.getInstance().getConfig().getBoolean(sec+".stand-up-when-damaged");
                if(standupwhendamage){
                    PosePlugin.getInstance().message().send(sec+".damage", p.getHandle());
                }
                break;
            }
            case DEAHTH:
                return;
            default:
                PosePlugin.getInstance().message().send(Message.STAND_UP, e.getPlayer().getHandle());

        }

    }

    @EventHandler
    public void onUsePotionThrowable(PlayerInteractEvent e){
        if(!PosePluginAPI.getAPI().getPlayerMap().containsPlayer(e.getPlayer())) return;
        PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(e.getPlayer().getName());
        if(e.getAction().equals(Action.RIGHT_CLICK_AIR)||e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            boolean preventInvisible = PosePlugin.getInstance().getConfig().getBoolean("lay.prevent-use-when-invisible");
            if (!p.getPoseType().equals(EnumPose.LYING)) return;
            if (preventInvisible) {
                if (e.getItem() == null) return;
                ItemStack hand = e.getItem();
                switch (hand.getType()) {
                    case POTION:
                    case LINGERING_POTION:
                    case SPLASH_POTION: {
                        PotionMeta meta = (PotionMeta) hand.getItemMeta();
                        if (meta != null) {
                            if (meta.getBasePotionData().getType().equals(PotionType.INVISIBILITY)) {
                                e.setCancelled(true);
                                PosePlugin.getInstance().message().send(Message.LAY_PREVENT_USE_POTION, e.getPlayer());
                            }
                        }
                    }
                    break;
                    default:
                        break;
                }
            }
        }
    }

    @EventHandler
    public void onUsePotionDrinkable(PlayerItemConsumeEvent e) {
        if(!PosePluginAPI.getAPI().getPlayerMap().containsPlayer(e.getPlayer())) return;
        PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(e.getPlayer().getName());
        boolean preventInvisible = PosePlugin.getInstance().getConfig().getBoolean("lay.prevent-use-when-invisible");
        if(!p.getPoseType().equals(EnumPose.LYING)) return;
        if (preventInvisible) {
            ItemStack hand = e.getItem();
            switch (hand.getType()) {
                case POTION:
                case LINGERING_POTION:
                case SPLASH_POTION: {
                    PotionMeta meta = (PotionMeta) hand.getItemMeta();
                    if (meta != null) {
                        if (meta.getBasePotionData().getType().equals(PotionType.INVISIBILITY)) {
                            e.setCancelled(true);
                            PosePlugin.getInstance().message().send(Message.LAY_PREVENT_USE_POTION, e.getPlayer());
                        }
                    }
                }
                break;
                default:
                    break;
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onEvent(EntityPotionEffectEvent event){
        if(!event.getEntity().getType().equals(EntityType.PLAYER)) return;
        Player player = (Player) event.getEntity();
        if(!PosePluginAPI.getAPI().getPlayerMap().containsPlayer(player)) return;
        PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(player.getName());
        if(!p.getPoseType().equals(EnumPose.LYING)) return;
        boolean preventInvisible = PosePlugin.getInstance().getConfig().getBoolean("lay.prevent-use-when-invisible");
        if(!preventInvisible){
            if(event.getAction().equals(EntityPotionEffectEvent.Action.ADDED)){
                p.getPose().getProperties().getProperty("invisible",Boolean.class).setValue(true);
            } else if(event.getAction().equals(EntityPotionEffectEvent.Action.REMOVED)||event.getAction().equals(EntityPotionEffectEvent.Action.CLEARED)){
                p.getPose().getProperties().getProperty("invisible",Boolean.class).setValue(false);
            }
        }
    }

    private String getSection(EnumPose pose){
        return pose.getName();
    }


}
