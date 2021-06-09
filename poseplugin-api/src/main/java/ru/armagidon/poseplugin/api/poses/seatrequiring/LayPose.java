package ru.armagidon.poseplugin.api.poses.seatrequiring;

import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.property.Property;
import ru.armagidon.poseplugin.api.utils.versions.PoseAvailabilitySince;

@PoseAvailabilitySince(version = "1.15")
public class LayPose extends SeatRequiringPose {

    private final FakePlayer fakePlayer;

    public LayPose(Player target) {
        super(target);
        this.fakePlayer = FakePlayer.createNew(target, Pose.SLEEPING);
        registerProperties();
    }

    private void registerProperties(){
        getProperties().registerProperty(EnumPoseOption.HEAD_ROTATION.mapper(), new Property<>(fakePlayer::isHeadRotationEnabled, fakePlayer::setHeadRotationEnabled))
                .registerProperty(EnumPoseOption.SWING_ANIMATION.mapper(),new Property<>(fakePlayer::isSwingAnimationEnabled, fakePlayer::setSwingAnimationEnabled))
                .registerProperty(EnumPoseOption.SYNC_EQUIPMENT.mapper(),new Property<>(fakePlayer::isSynchronizationEquipmentEnabled, fakePlayer::setSynchronizationEquipmentEnabled))
                .registerProperty(EnumPoseOption.SYNC_OVERLAYS.mapper(),new Property<>(fakePlayer::isSynchronizationOverlaysEnabled, fakePlayer::setSynchronizationOverlaysEnabled))
                .registerProperty(EnumPoseOption.VIEW_DISTANCE.mapper(),new Property<>(fakePlayer::getViewDistance, fakePlayer::setViewDistance))
                .registerProperty(EnumPoseOption.INVISIBLE.mapper(),new Property<>(fakePlayer::isInvisible, fakePlayer::setInvisible))
                .register();
    }

    @Override
    public void initiate() {
        super.initiate();
        fakePlayer.initiate();
        PosePluginAPI.getAPI().getPlayerHider().hide(getPlayer());
        PosePluginAPI.getAPI().getNameTagHider().hideTag(getPlayer());
        PosePluginAPI.getAPI().getArmorHider().hideArmor(getPlayer());
    }

    @Override
    public void play(Player receiver) {
        super.play(receiver);
        if(receiver == null)
            fakePlayer.broadCastSpawn();
        else fakePlayer.spawnToPlayer(receiver);
    }

    @Override
    public void stop() {
        super.stop();
        fakePlayer.remove();
        fakePlayer.destroy();
        PosePluginAPI.getAPI().getPlayerHider().show(getPlayer());
        PosePluginAPI.getAPI().getNameTagHider().showTag(getPlayer());
        PosePluginAPI.getAPI().getArmorHider().showArmor(getPlayer());
    }

    @Override
    public EnumPose getType() {
        return EnumPose.LYING;
    }

    @Override
    public void handleTeleport(ArmorStandSeat seat) {
        fakePlayer.teleport(getPlayer().getLocation());
    }
}
