package ru.armagidon.poseplugin.api.poses.swim.module;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.utils.nms.FakePlayer;

public class WaterSwimModule implements SwimModule
{

    private final Player player;

    public WaterSwimModule(Player player) {
        this.player = player;
        Bukkit.getServer().getPluginManager().registerEvents(this, PosePlugin.getInstance());
    }

    @Override
    public void action() {
        player.setSwimming(false);
        player.setSprinting(false);
        player.setSprinting(true);
        player.setSwimming(true);
    }

    @EventHandler
    public void onSwim(EntityToggleSwimEvent event){
        if(event.getEntity().equals(player)){
            if(!event.isSwimming()){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSprint(PlayerToggleSprintEvent event){
        if(event.getPlayer().equals(player)){
            if(!event.isSprinting()){
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        player.setSwimming(false);
        player.setSprinting(false);
    }

    @Override
    public SwimModuleType getType() {
        return SwimModuleType.WATER;
    }

    public static boolean test(Player player){
        return FakePlayer.asNMSCopy(player).isInWater();
    }
}
