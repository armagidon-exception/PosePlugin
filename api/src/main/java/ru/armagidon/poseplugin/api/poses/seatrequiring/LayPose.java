package ru.armagidon.poseplugin.api.poses.sit;

import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.inventory.MainHand;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.poses.AbstractPose;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.poses.sit.ArmorStandSeat;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.property.Property;

public class LayPose extends AbstractPose {

    private final FakePlayer fakePlayer;
    private final ArmorStandSeat driver;

    public LayPose(Player target) {
        super(target);
        this.fakePlayer = FakePlayer.createNew(target, Pose.SLEEPING);
        registerProperties();
        this.driver = new ArmorStandSeat(target, (e,a)-> {
            if(!getPosePluginPlayer().resetCurrentPose()) {
                e.setCancelled(true);
                a.pushBack();
            }
        });
        this.driver.setTeleport((event, seat) -> {
            fakePlayer.teleport(getPlayer().getLocation());
        });
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
        driver.takeASeat();
        PosePluginAPI.getAPI().getPlayerHider().hide(getPlayer());
        PosePluginAPI.getAPI().getNameTagHider().hideTag(getPlayer());
        PosePluginAPI.getAPI().getArmorHider().hideArmor(getPlayer());
    }

    @Override
    public void play(Player receiver) {
        if(receiver == null)
            fakePlayer.broadCastSpawn();
        else fakePlayer.spawnToPlayer(receiver);
    }

    @Override
    public void stop() {
        super.stop();
        fakePlayer.remove();
        fakePlayer.destroy();
        driver.standUp();
        PosePluginAPI.getAPI().getPlayerHider().show(getPlayer());
        PosePluginAPI.getAPI().getNameTagHider().showTag(getPlayer());
        PosePluginAPI.getAPI().getArmorHider().showArmor(getPlayer());
    }

    @Override
    public EnumPose getType() {
        return EnumPose.LYING;
    }

    @EventHandler
    public void onArmSwing(PlayerAnimationEvent event){
        if(event.getPlayer().equals(getPlayer())){
            fakePlayer.swingHand(event.getPlayer().getMainHand().equals(MainHand.RIGHT));
        }
    }
}
