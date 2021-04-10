package ru.armagidon.poseplugin.api.poses.swim;

import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.spigotmc.event.entity.EntityMountEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.poses.AbstractPose;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.swim.module.LandModule;
import ru.armagidon.poseplugin.api.poses.swim.module.SwimModule;
import ru.armagidon.poseplugin.api.poses.swim.module.WaterModule;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.versions.PoseAvailabilitySince;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

@PoseAvailabilitySince(version = "1.15")
public class SwimPose extends AbstractPose implements Tickable {

    private SwimModule module;

    public SwimPose(Player target) {
        super(target);
        registerProperties();
    }

    private void registerProperties(){
        getProperties().register();
    }


    @Override
    public void initiate() {
        super.initiate();
        PosePluginAPI.getAPI().getTickingBundle().addToTickingBundle(SwimPose.class, this);
        if(isInWater(getPlayer())){
            module = new WaterModule(getPosePluginPlayer());
        } else {
            module = new LandModule(getPosePluginPlayer());
        }
    }

    @Override
    public void play(Player receiver) {
        module.play();
    }

    @Override
    public void stop() {
        super.stop();
        module.stop();
        PosePluginAPI.getAPI().getTickingBundle().removeFromTickingBundle(SwimPose.class, this);
    }

    @Override
    public EnumPose getType() {
        return EnumPose.SWIMMING;
    }

    @EventHandler
    public void onMount(EntityMountEvent event){
        if(event.getEntity().equals(getPlayer())){
            getPosePluginPlayer().resetCurrentPose();
        }
    }

    @EventHandler
    public void onFly(PlayerToggleFlightEvent event){
        if (event.getPlayer().equals(getPlayer())) {
            getPosePluginPlayer().stopPosingSilently();
        }
    }

    @Override
    public void tick() {
        if(isInWater(getPlayer())){
            if(!module.getMode().equals(SwimMode.SWIMMING)) {
                module.stop();
                module = new WaterModule(getPosePluginPlayer());
                module.play();
            }
        } else {
            if(!module.getMode().equals(SwimMode.CRAWLING)) {
                module.stop();
                module = new LandModule(getPosePluginPlayer());
                module.play();
            }
        }
        module.tick();
    }

    public enum SwimMode{
        FLYING,
        SWIMMING,
        CRAWLING
    }

    @SneakyThrows
    private boolean isInWater(Player player){
        return (boolean) ReflectionTools.getNmsClass("Entity").getDeclaredMethod("isInWater").invoke(NMSUtils.asNMSCopy(player)); //FakePlayer.asNMSCopy(player).isInWater();
    }
}