package ru.armagidon.poseplugin.api.poses.sit;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;

public abstract class SitPose extends PluginPose {

    public SitPose(Player player) {
        super(player);
    }

    public void play(Player receiver,boolean log){
        super.play(receiver,log);
        takeASeat(getPlayer(),getPlayer().getLocation());
    }

    @Override
    public void stop(boolean log){
        super.stop(log);
        standUp(getPlayer());
    }

    public abstract void takeASeat(Player player, Location l);

    public abstract void standUp(Player player);

    @Override
    public EnumPose getPose() {
        return EnumPose.SITTING;
    }

    public static SitPose getInstance(Player player){
        if(PosePlugin.bridge!=null){
            return new ExternalSitPose(player);
        } else {
            return new InternalSitPose(player);
        }
    }

}
