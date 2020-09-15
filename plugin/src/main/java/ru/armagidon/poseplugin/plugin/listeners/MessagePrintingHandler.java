package ru.armagidon.poseplugin.plugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.HandTypeChangeEvent;
import ru.armagidon.poseplugin.api.events.PoseChangeEvent;
import ru.armagidon.poseplugin.api.events.PostPoseChangeEvent;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.plugin.configuration.messaging.Message;

public class MessagePrintingHandler implements Listener
{

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStop(StopAnimationEvent e){
        PosePluginPlayer p = e.getPlayer();
        if(p.getPose().isAPIModeActivated()) return;
        if(p.getPose().getPose().equals(EnumPose.STANDING)) return;
        String cause = e.getCustomCause();
        switch (cause){
            case "DAMAGE":{
                PosePlugin.getInstance().message().send(e.getPose().getName()+".damage", p.getHandle());
                break;
            }
            case "DEATH":
                return;
            default:
                PosePlugin.getInstance().message().send(e.getPose().getName()+".stop", e.getPlayer().getHandle());

        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnimationStart(PostPoseChangeEvent event){
        if(event.getPlayer().getPose().isAPIModeActivated()) return;
        String sec = event.getPose().getName();
        PosePlugin.getInstance().message().send(sec+".play",event.getPlayer().getHandle());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void preventInvisibility(PoseChangeEvent event){
        if(event.isCancelled()) return;
        if(event.getPlayer().getPose().isAPIModeActivated()) return;
        if(event.getAfter().equals(EnumPose.LYING)){
            Player player = event.getPlayer().getHandle();
            boolean prevent_invisible = PosePlugin.getInstance().getConfig().getBoolean("lay.prevent-use-when-invisible");
            if(prevent_invisible&&player.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                PosePlugin.getInstance().message().send(Message.LAY_PREVENT_INVISIBILITY, player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onUsePotionDrinkable(PlayerItemConsumeEvent e) {
        if(!PosePluginAPI.getAPI().getPlayerMap().containsPlayer(e.getPlayer())) return;
        PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(e.getPlayer().getName());
        boolean preventInvisible = PosePlugin.getInstance().getConfig().getBoolean("lay.prevent-use-when-invisible");
        if(!p.getPoseType().equals(EnumPose.LYING)) return;
        if(p.getPose().isAPIModeActivated()) return;
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
                    break;
                }
                default:
                    break;
            }
        }
    }

    @EventHandler
    public void onUsePotionThrowable(PlayerInteractEvent e){
        if(!PosePluginAPI.getAPI().getPlayerMap().containsPlayer(e.getPlayer())) return;
        PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(e.getPlayer().getName());
        if(e.getAction().equals(Action.RIGHT_CLICK_AIR)||e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            boolean preventInvisible = PosePlugin.getInstance().getConfig().getBoolean("lay.prevent-use-when-invisible");
            if (!p.getPoseType().equals(EnumPose.LYING)) return;
            if(p.getPose().isAPIModeActivated()) return;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHandModeChange(HandTypeChangeEvent event){
        if(event.isCancelled()) return;
        if(event.getPlayer().getPose().isAPIModeActivated()) return;
        PosePlugin.getInstance().message().send(event.getPose().getName()+".handmode-change", event.getPlayer().getHandle());
    }
}
