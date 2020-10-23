package ru.armagidon.poseplugin.plugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.PoseChangeEvent;
import ru.armagidon.poseplugin.api.events.PostPoseChangeEvent;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.plugin.configuration.ConfigCategory;
import ru.armagidon.poseplugin.plugin.configuration.messaging.Message;
import ru.armagidon.poseplugin.plugin.configuration.settings.LaySettings;
import ru.armagidon.poseplugin.plugin.events.HandTypeChangeEvent;
import ru.armagidon.poseplugin.plugin.events.StopAnimationWithMessageEvent;

import static ru.armagidon.poseplugin.PosePlugin.PLAYERS_POSES;

public class MessagePrintingHandler implements Listener
{

    @EventHandler(priority = EventPriority.MONITOR)
    public void onStop(StopAnimationWithMessageEvent e){
        PosePluginPlayer p = e.getPlayer();
        if( !PLAYERS_POSES.containsKey(p.getHandle()) ) return;
        if(p.getPose().getType().equals(EnumPose.STANDING)) return;
        switch (e.getCause()){
            case DAMAGE:
                PosePlugin.getInstance().message().send(e.getPose().getName()+".damage", p.getHandle());
                break;
            case OTHER:
                PosePlugin.getInstance().message().send(e.getPose().getName()+".stop", e.getPlayer().getHandle());
                break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnimationStart(PostPoseChangeEvent event){

        if( !PLAYERS_POSES.containsKey(event.getPlayer().getHandle()) ) return;

        String sec = event.getPoseType().getName();
        PosePlugin.getInstance().message().send(sec + ".play",event.getPlayer().getHandle());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void preventInvisibility(PoseChangeEvent event){

        if( !PLAYERS_POSES.containsKey(event.getPlayer().getHandle()) ) return;

        if( event.getNewPose().getType().equals(EnumPose.LYING) ){
            Player player = event.getPlayer().getHandle();
            boolean preventInvisible = PosePlugin.getInstance().getConfigManager().get(ConfigCategory.LAY, LaySettings.PREVENT_USE_WHEN_INVISIBLE);
            if(preventInvisible && player.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                PosePlugin.getInstance().message().send(Message.LAY_PREVENT_INVISIBILITY, player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onUsePotionDrinkable(PlayerItemConsumeEvent e) {
        handlePotion(e, e.getPlayer(), e.getItem());
    }

    @EventHandler
    public void onUsePotionThrowable(PlayerInteractEvent e){
        handlePotion(e, e.getPlayer(), e.getItem());
    }

    private void handlePotion(Cancellable cancellable, Player player, ItemStack hand){
        if( !PLAYERS_POSES.containsKey(player) ) return;

        PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(player);
        boolean preventInvisible = PosePlugin.getInstance().getConfig().getBoolean("lay.prevent-use-when-invisible");
        if( !p.getPoseType().equals(EnumPose.LYING) ) return;
        if (preventInvisible) {

            if ( hand.getType() == Material.POTION || hand.getType() == Material.LINGERING_POTION || hand.getType() == Material.SPLASH_POTION ){

                PotionMeta meta = (PotionMeta) hand.getItemMeta();
                if (meta != null) {
                    if (meta.getBasePotionData().getType() == PotionType.INVISIBILITY) {
                        cancellable.setCancelled(true);
                        PosePlugin.getInstance().message().send(Message.LAY_PREVENT_USE_POTION, player);
                    }
                }

            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHandModeChange(HandTypeChangeEvent event){
        if( !PLAYERS_POSES.containsKey(event.getPlayer().getHandle()) ) return;
        PosePlugin.getInstance().message().send(event.getPose().getName()+".handmode-change", event.getPlayer().getHandle());
    }
}
