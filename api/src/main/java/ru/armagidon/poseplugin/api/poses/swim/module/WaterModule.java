package ru.armagidon.poseplugin.api.poses.swim.module;

import org.bukkit.event.HandlerList;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.swim.SwimPose;

public class WaterModule extends SwimModule {

    public WaterModule(PosePluginPlayer target) {
        super(target);
    }

    @Override
    public void play() {
        getTarget().getHandle().setSprinting(true);
        getTarget().getHandle().setSwimming(true);
    }

    @Override
    public void tick() {
        getTarget().getHandle().setSwimming(false);
        getTarget().getHandle().setSprinting(false);
        getTarget().getHandle().setSprinting(true);
        getTarget().getHandle().setSwimming(true);
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        getTarget().getHandle().setSwimming(false);
        getTarget().getHandle().setSprinting(false);
    }

    @Override
    public SwimPose.SwimMode getMode() {
        return SwimPose.SwimMode.SWIMMING;
    }
}
