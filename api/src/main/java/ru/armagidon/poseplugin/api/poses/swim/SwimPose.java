package ru.armagidon.poseplugin.api.poses.swim;

import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.spigotmc.event.entity.EntityMountEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.poses.swim.module.LandModule;
import ru.armagidon.poseplugin.api.poses.swim.module.SwimModule;
import ru.armagidon.poseplugin.api.poses.swim.module.WaterModule;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;
import ru.armagidon.poseplugin.api.utils.property.Property;

import java.util.concurrent.atomic.AtomicReference;

public class SwimPose extends PluginPose implements Tickable {

    private final AtomicReference<Boolean> _static;
    private SwimModule module;

    public SwimPose(Player target) {
        super(target);
        registerProperties();
        this._static = new AtomicReference<>(getProperties().getProperty("static",Boolean.class).getValue());
    }

    private void registerProperties(){
        getProperties().registerProperty("static", new Property<>(false, this::setStatic));
        getProperties().register();
    }


    @Override
    public void initiate() {
        super.initiate();
        PosePluginAPI.getAPI().getTickManager().registerTickModule(this, false);
        if(isInWater(getPlayer())){
            module = new WaterModule(getPosePluginPlayer());
        } else {
            module = new LandModule(getPosePluginPlayer(), _static);
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
        PosePluginAPI.getAPI().getTickManager().removeTickModule(this);
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.SWIMMING;
    }

    @EventHandler
    public void onMount(EntityMountEvent event){
        if(event.getEntity().equals(getPlayer())){
            callStopEvent(getPosePluginPlayer().getPoseType(), getPosePluginPlayer(), StopAnimationEvent.StopCause.STOPPED);
        }
    }

    @EventHandler
    public void onFly(PlayerToggleFlightEvent event){
        if (event.getPlayer().equals(getPlayer())) {
            PluginPose.callStopEvent(getPosePluginPlayer().getPoseType(), getPosePluginPlayer(), StopAnimationEvent.StopCause.STOPPED);
        }
    }

    public void setStatic(boolean _static) {
        this._static.set(_static);
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
                module = new LandModule(getPosePluginPlayer(), _static);
                module.play();
            }
        }
        module.tick();
    }

    public enum SwimMode{
        FLYING,SWIMMING, CRAWLING
    }

    @SneakyThrows
    private boolean isInWater(Player player){
        return (boolean) ReflectionTools.getNmsClass("Entity").getDeclaredMethod("isInWater").invoke(NMSUtils.asNMSCopy(player)); //FakePlayer.asNMSCopy(player).isInWater();
    }
}