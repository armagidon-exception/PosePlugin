package ru.armagidon.poseplugin.api.poses.swim;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;

public abstract class SwimPose extends PluginPose {

    public SwimPose(Player target) {
        super(target);
        initTickModules();
    }

    public static SwimPose newInstance(Player player) {
        final boolean STATIC = PosePlugin.getInstance().getConfig().getBoolean("swim.static");
        if(STATIC){
            return new StaticSwimPose(player);
        } else {
            return new NonStaticSwimPose(player);
        }
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.SWIMMING;
    }

    @Override
    public String getSectionName() {
        return "swim";
    }

    @PersonalEventHandler
    public void onSneak(PlayerToggleSneakEvent event){
        //Call stop event
        if (getPosePluginPlayer().getPoseType().equals(EnumPose.SWIMMING)&&event.getPlayer().isOnGround()) {
            PluginPose.callStopEvent(getPosePluginPlayer().getPoseType(), getPosePluginPlayer(), true, StopAnimationEvent.StopCause.STOPPED);
        }
    }
}