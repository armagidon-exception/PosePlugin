package ru.armagidon.poseplugin.api.poses.lay;

import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
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
        fakePlayer.setHeadRotationEnabled(getProperties().getProperty("head-rotation",Boolean.class).getValue());
        fakePlayer.setSwingAnimationEnabled(getProperties().getProperty("swing-animation",Boolean.class).getValue());
        fakePlayer.setUpdateEquipmentEnabled(getProperties().getProperty("update-equipment",Boolean.class).getValue());
        fakePlayer.setUpdateOverlaysEnabled(getProperties().getProperty("update-overlays",Boolean.class).getValue());
        fakePlayer.setViewDistance(getProperties().getProperty("view-distance",Integer.class).getValue());
        this.driver = new SitDriver(target, (e)-> {
            if(!callStopEvent(EnumPose.LYING, getPosePluginPlayer(), StopAnimationEvent.StopCause.STOPPED)){
                e.setCancelled(true);
            }
        });
    }

    private void registerProperties(){
        getProperties().registerProperty("head-rotation", new Property<>(true, fakePlayer::setHeadRotationEnabled));
        getProperties().registerProperty("swing-animation",new Property<>(true, fakePlayer::setSwingAnimationEnabled));
        getProperties().registerProperty("update-equipment",new Property<>(true, fakePlayer::setUpdateEquipmentEnabled));
        getProperties().registerProperty("update-overlays",new Property<>(true, fakePlayer::setUpdateOverlaysEnabled));
        getProperties().registerProperty("view-distance",new Property<>(20, fakePlayer::setViewDistance));
        getProperties().registerProperty("invisible",new Property<>(false, fakePlayer::setInvisible));

        getProperties().register();
    }
    @Override
    public void initiate() {
        fakePlayer.initiate();
        driver.takeASeat();
        PosePluginAPI.getAPI().getPlayerHider().hide(getPlayer());
    }

    @Override
    public void play(Player receiver) {
        if(receiver==null) {
            fakePlayer.broadCastSpawn();
        } else {
            fakePlayer.spawnToPlayer(receiver);
        }
    }

    @Override
    public void stop() {
        super.stop();
        fakePlayer.remove();
        fakePlayer.destroy();
        driver.standUp();
        PosePluginAPI.getAPI().getPlayerHider().show(getPlayer());
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.LYING;
    }
}
