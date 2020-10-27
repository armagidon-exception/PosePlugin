package ru.armagidon.poseplugin.api.poses.lay;

import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.MainHand;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.AbstractPose;
import ru.armagidon.poseplugin.api.poses.EnumPose;
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
            if(!getPosePluginPlayer().resetCurrentPose(true)) {
                e.setCancelled(true);
                a.pushBack();
            }
        });
    }

    private void registerProperties(){
        getProperties().registerProperty("head-rotation", new Property<>(fakePlayer::isHeadRotationEnabled, fakePlayer::setHeadRotationEnabled))
                .registerProperty("swing-animation",new Property<>(fakePlayer::isSwingAnimationEnabled, fakePlayer::setSwingAnimationEnabled))
                .registerProperty("sync-equipment",new Property<>(fakePlayer::isSynchronizationEquipmentEnabled, fakePlayer::setSynchronizationEquipmentEnabled))
                .registerProperty("sync-overlays",new Property<>(fakePlayer::isSynchronizationOverlaysEnabled, fakePlayer::setSynchronizationOverlaysEnabled))
                .registerProperty("view-distance",new Property<>(fakePlayer::getViewDistance, fakePlayer::setViewDistance))
                .registerProperty("invisible",new Property<>(fakePlayer::isInvisible, fakePlayer::setInvisible))
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
    public EnumPose getType() {
        return EnumPose.LYING;
    }

    @EventHandler
    public void onArmSwing(PlayerAnimationEvent event){
        if(event.getPlayer().equals(getPlayer())){
            fakePlayer.swingHand(event.getPlayer().getMainHand().equals(MainHand.RIGHT));
        }
    }

    @PersonalEventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        fakePlayer.remove();
        fakePlayer.setPosition(event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
        fakePlayer.updateNPC();
    }
}
