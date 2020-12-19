package ru.armagidon.poseplugin.api.poses.sit;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.poses.AbstractPose;
import ru.armagidon.poseplugin.api.poses.EnumPose;

public class SitPose extends AbstractPose {

    private final ArmorStandSeat driver;

    public SitPose(Player player) {
        super(player);
        this.driver = new ArmorStandSeat(player, (e,s)-> {
            if(!getPosePluginPlayer().resetCurrentPose()){
                e.setCancelled(true);
                s.pushBack();
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
    public EnumPose getType() {
        return EnumPose.SITTING;
    }

    @Override
    public void initiate() {
        super.initiate();
        getProperties().register();
    }
}
