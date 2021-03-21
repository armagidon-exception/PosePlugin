package ru.armagidon.poseplugin.api.poses.seatrequiring;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.poses.AbstractPose;
import ru.armagidon.poseplugin.api.poses.EnumPose;

public class SitPose extends SeatRequiringPose {

    public SitPose(Player player) {
        super(player);
    }

    public void play(Player receiver){
        super.play(receiver);
    }

    @Override
    public void stop(){
        super.stop();
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
