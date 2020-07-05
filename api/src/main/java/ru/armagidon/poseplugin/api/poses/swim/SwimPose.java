package ru.armagidon.poseplugin.api.poses.swim;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.poses.swim.module.NonStaticSwimPose;
import ru.armagidon.poseplugin.api.poses.swim.module.StaticSwimPose;
import ru.armagidon.poseplugin.api.poses.swim.module.SwimModule;
import ru.armagidon.poseplugin.api.poses.swim.module.WaterSwimModule;
import ru.armagidon.poseplugin.api.ticking.Tickable;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;
import ru.armagidon.poseplugin.api.utils.property.Property;

public class SwimPose extends PluginPose {

    private SwimModule impl;
    private boolean _static;

    private final Tickable tickable;

    public SwimPose(Player target) {
        super(target);
        registerProperties();
        this._static = getProperties().getProperty("static",Boolean.class).getValue();
        if(_static){
            setMode(new StaticSwimPose(getPlayer()));
        } else {
            setMode(new NonStaticSwimPose(getPlayer()));
        }
        tickable = this::tick;
    }

    private void registerProperties(){
        getProperties().registerProperty("static", new Property<>(false, this::setStatic));
        getProperties().register();
    }

    private void tick(){
        if(!_static) {
            Bukkit.getOnlinePlayers().stream().filter(PosePluginAPI.getAPI().getPlayerMap()::containsPlayer)
                    .map(p -> PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(p.getName())).filter(p -> p.getPoseType().equals(EnumPose.SWIMMING)).forEach(p -> {
                if (isInWater(p.getHandle())) {
                    if (impl.getMode().equals(SwimPose.SwimMode.SWIMMING)) return;
                    setMode(new WaterSwimModule(p.getHandle()));
                } else {
                    if (impl.getMode().equals(SwimPose.SwimMode.MOVING)) return;
                    setMode(new NonStaticSwimPose(p.getHandle()));
                }
            });
        } else {
            if(impl.getMode().equals(SwimMode.STATIC)) return;
            setMode(new StaticSwimPose(getPlayer()));
        }
        impl.action();
    }

    @Override
    public void initiate() {
        super.initiate();
        PosePluginAPI.getAPI().getTickManager().registerTickModule(tickable, false);
    }

    @Override
    public void play(Player receiver) {

    }

    @Override
    public void stop() {
        super.stop();
        PosePluginAPI.getAPI().getTickManager().removeTickModule(tickable);
        impl.stop();
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.SWIMMING;
    }

    @PersonalEventHandler
    public void onSneak(PlayerToggleSneakEvent event){
        //Call stop event
        if (getPosePluginPlayer().getPoseType().equals(EnumPose.SWIMMING)&&event.getPlayer().isOnGround()) {
            PluginPose.callStopEvent(getPosePluginPlayer().getPoseType(), getPosePluginPlayer(), StopAnimationEvent.StopCause.STOPPED);
        }
    }

    public void setStatic(boolean _static) {
        this._static = _static;
    }

    public void setMode(SwimModule impl) {
        if(this.impl!=null) this.impl.stop();
        this.impl = impl;
    }

    public enum SwimMode{
        FLYING,SWIMMING, MOVING, STATIC
    }

    @SneakyThrows
    private boolean isInWater(Player player){
        return (boolean) ReflectionTools.getNmsClass("Entity").getDeclaredMethod("isInWater").invoke(NMSUtils.asNMSCopy(player)); //FakePlayer.asNMSCopy(player).isInWater();
    }
}