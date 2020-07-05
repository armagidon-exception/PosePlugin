package ru.armagidon.poseplugin.api.poses.sit;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;

public class SitPose extends PluginPose {

    private final SitDriver driver;

    public SitPose(Player player) {
        super(player);
        this.driver = new SitDriver(player, (e)-> {
            if(!callStopEvent(EnumPose.LYING, getPosePluginPlayer(), StopAnimationEvent.StopCause.STOPPED)){
                e.setCancelled(true);
            }
        });
    }

    public void play(Player receiver){
        driver.takeASeat();
    }

    @Override
    public void stop(){
        super.stop();
        driver.standUp();
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.SITTING;
    }

    @Override
    public void initiate() {
        getProperties().register();
    }
}
