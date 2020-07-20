package ru.armagidon.poseplugin.api.poses;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.personalListener.PersonalListener;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.api.utils.property.PropertyMap;

public abstract class PluginPose implements IPluginPose,Listener, PersonalListener
{
    private final Player player;
    private final PropertyMap propertyMap;

    public static IPluginPose standing = new StandingPose();

    public PluginPose(Player target) {
        this.player = target;
        this.propertyMap = new PropertyMap();
    }

    public Player getPlayer() {
        return player;
    }

    protected final PosePluginPlayer getPosePluginPlayer(){
        return PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(player.getName());
    }

    @Override
    public void initiate() {
        Bukkit.getPluginManager().registerEvents(this, PosePluginAPI.getAPI().getPlugin());
    }

    public void stop(){
        HandlerList.unregisterAll(this);
        getPosePluginPlayer().setPose(PluginPose.standing);
    }

    @Override
    public PropertyMap getProperties() {
        return propertyMap;
    }

    //If event was cancelled - return false
    public static boolean callStopEvent(EnumPose pose, PosePluginPlayer player, StopAnimationEvent.StopCause cause){
        StopAnimationEvent stopevent = new StopAnimationEvent(pose, player, cause);
        Bukkit.getPluginManager().callEvent(stopevent);
        if(stopevent.isCancelled()) return false;
        player.getPose().stop();
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public final void onBlockBreak(BlockBreakEvent event) {
        Block under = VectorUtils.getBlock(getPlayer().getLocation());
        if (event.getBlock().equals(under)) {
            callStopEvent(getPose(), getPosePluginPlayer(), StopAnimationEvent.StopCause.BLOCK_UPDATE);
        }
    }

    @SuppressWarnings("unused")
    @PersonalEventHandler
    public final void onDamage(EntityDamageEvent event){
        callStopEvent(getPose(), getPosePluginPlayer(), StopAnimationEvent.StopCause.DAMAGE);
    }

    @SuppressWarnings("unused")
    @PersonalEventHandler
    public final void gameMode(PlayerGameModeChangeEvent event){
        if(event.getNewGameMode().equals(GameMode.SPECTATOR)){
            callStopEvent(getPose(),getPosePluginPlayer(), StopAnimationEvent.StopCause.GAMEMODE_CHANGE);
        }
    }

    @SuppressWarnings("unused")
    @PersonalEventHandler
    public final void onTeleport(PlayerTeleportEvent event){
        Location from = event.getFrom();
        Location to = event.getTo();
        if(to!=null&&to.distanceSquared(from)>1){
            callStopEvent(getPose(), getPosePluginPlayer(),StopAnimationEvent.StopCause.TELEPORT);
        }
    }

    @PersonalEventHandler
    public final void onDeath(PlayerDeathEvent event){
        callStopEvent(getPose(), getPosePluginPlayer(), StopAnimationEvent.StopCause.DEATH);
    }

    private static class StandingPose implements IPluginPose
    {

        private final PropertyMap propertyMap = new PropertyMap();

        public StandingPose() {}

        @Override
        public PropertyMap getProperties() {
            return propertyMap;
        }

        @Override
        public void initiate() {}

        @Override
        public void play(Player receiver) {}

        @Override
        public void stop() {}

        @Override
        public EnumPose getPose() {
            return EnumPose.STANDING;
        }
    }
}
