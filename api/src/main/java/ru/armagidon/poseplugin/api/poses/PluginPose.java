package ru.armagidon.poseplugin.api.poses;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.personalListener.PersonalListener;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.utils.property.PropertyMap;

public abstract class PluginPose implements IPluginPose,Listener, PersonalListener
{
    private final Player player;
    private final PropertyMap propertyMap;
    private boolean apiMode;

    public static IPluginPose standing = new StandingPose();

    public PluginPose(Player target) {
        this.player = target;
        this.propertyMap = new PropertyMap();
        this.apiMode = false;
    }

    public Player getPlayer() {
        return player;
    }

    protected final PosePluginPlayer getPosePluginPlayer(){
        return PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(player.getName());
    }

    @Override
    public void initiate() {
        PosePluginAPI.getAPI().getPersonalHandlerList().subscribe(getPosePluginPlayer(), this);
        Bukkit.getPluginManager().registerEvents(this, PosePluginAPI.getAPI().getPlugin());
    }

    public void stop(){
        PosePluginAPI.getAPI().getPersonalHandlerList().unsubscribe(getPosePluginPlayer(), this);
        HandlerList.unregisterAll(this);
        getPosePluginPlayer().setPose(PluginPose.standing);
    }

    @Override
    public PropertyMap getProperties() {
        return propertyMap;
    }

    //If event was cancelled - return false
    public static boolean callStopEvent(EnumPose pose, PosePluginPlayer player, StopAnimationEvent.StopCause cause, String custom){
        StopAnimationEvent stopEvent = new StopAnimationEvent(pose, player, cause, custom);
        Bukkit.getPluginManager().callEvent(stopEvent);
        if(stopEvent.isCancelled()&&!cause.equals(StopAnimationEvent.StopCause.QUIT)) return false;
        player.getPose().stop();
        return true;
    }

    public static boolean callStopEvent(EnumPose pose, PosePluginPlayer player, StopAnimationEvent.StopCause cause){
        return callStopEvent(pose,player,cause,cause.name());
    }

    @PersonalEventHandler
    public void onDeath(PlayerDeathEvent e){
        callStopEvent(getPose(), getPosePluginPlayer(), StopAnimationEvent.StopCause.STOPPED, "DEATH");
    }


    @Override
    public final boolean isAPIModeActivated() {
        return apiMode;
    }

    @Override
    public final void setAPIMode(boolean mode) {
        this.apiMode = mode;
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

        @Override
        public boolean isAPIModeActivated() {
            return false;
        }

        @Override
        public void setAPIMode(boolean mode) {
            throw new UnsupportedOperationException("Cannot set api mode of standing pose");
        }
    }
}
