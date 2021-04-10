package ru.armagidon.poseplugin.api.poses.experimental;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.AbstractPose;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.versions.PoseAvailabilitySince;
import ru.armagidon.poseplugin.api.utils.property.Property;

@PoseAvailabilitySince(version = "1.15")
public class PrayPose extends AbstractPose implements Tickable {

    @Getter
    private float step;

    public PrayPose(Player target) {
        super(target);
        getProperties().registerProperty(EnumPoseOption.STEP.mapper(), new Property<>(this::getStep, this::setStep));
        getProperties().register();
    }

    @Override
    public void initiate() {
        super.initiate();
        PosePluginAPI.getAPI().getTickManager().registerTickModule(this, false);
    }

    @Override
    public void play(Player receiver) {

    }

    @Override
    public void stop() {
        super.stop();
        PosePluginAPI.getAPI().getTickManager().removeTickModule(this);
        getPlayer().setGliding(false);
    }

    @PersonalEventHandler
    public void onMove(PlayerMoveEvent e) {

    }

    @Override
    public EnumPose getType() {
        return EnumPose.PRAYING;
    }

    public void setStep(float step) {
        this.step = step;
    }

    private boolean up = false;

    @Override
    public void tick() {
        getPlayer().setGliding(true);

        float stepMovement = step;

        Location location = getPlayer().getLocation();

        if (up) {
            if (location.getPitch() <= -90) up = false;
        } else {
            if (location.getPitch() >= 0) up = true;
        }

        stepMovement *= up ? -1 : 1;

        location.setPitch(location.getPitch() + stepMovement);
        location.setYaw(location.getYaw());
        getPlayer().teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }
}
