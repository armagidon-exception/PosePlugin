package ru.armagidon.poseplugin.api.poses.lay;

import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.player.PlayerTeleportEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.poses.sit.SitDriver;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.property.Property;

public class LayPose extends PluginPose {

    private final FakePlayer fakePlayer;
    private final SitDriver driver;

    public LayPose(Player target) {
        super(target);
        this.fakePlayer = PosePluginAPI.getAPI().getNMSFactory().createFakePlayer(getPlayer(), Pose.SLEEPING);
        registerProperties();
        this.driver = new SitDriver(target, (e)-> {
            if(!callStopEvent(EnumPose.LYING, getPosePluginPlayer(), StopAnimationEvent.StopCause.STOPPED)) e.setCancelled(true);
        });
    }

    private void registerProperties(){
        getProperties().registerProperty("head-rotation", new Property<>(fakePlayer::isHeadRotationEnabled, fakePlayer::setHeadRotationEnabled));
        getProperties().registerProperty("swing-animation",new Property<>(fakePlayer::isSwingAnimationEnabled, fakePlayer::setSwingAnimationEnabled));
        getProperties().registerProperty("update-equipment",new Property<>(fakePlayer::isUpdateEquipmentEnabled, fakePlayer::setUpdateEquipmentEnabled));
        getProperties().registerProperty("update-overlays",new Property<>(fakePlayer::isUpdateOverlaysEnabled, fakePlayer::setUpdateOverlaysEnabled));
        getProperties().registerProperty("view-distance",new Property<>(fakePlayer::getViewDistance, fakePlayer::setViewDistance));
        getProperties().registerProperty("invisible",new Property<>(fakePlayer::isInvisible, fakePlayer::setInvisible));

        getProperties().register();
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
        if(receiver==null) fakePlayer.broadCastSpawn();
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
    public EnumPose getPose() {
        return EnumPose.LYING;
    }

    @PersonalEventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if(event.isCancelled()) return;
        fakePlayer.remove();
        fakePlayer.setPosition(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
        fakePlayer.updateNPC();
    }
}
