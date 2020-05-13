package ru.armagidon.poseplugin;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.poses.*;
import ru.armagidon.poseplugin.poses.sit.SitPose;
import ru.armagidon.poseplugin.poses.swim.SwimPose;

//SitPlugin player
public class PosePluginPlayer
{
    private final Player player;

    private IPluginPose pose;

    public PosePluginPlayer(Player player) {
        this.player = player;
        this.pose = new StandingPose();
    }

    public Player getPlayer() {
        return player;
    }

    public void setPose(IPluginPose newpose) {
        this.pose = newpose;
    }

    public void changePose(EnumPose pose){
        this.pose.stop(true);
        IPluginPose newpose;
        switch (pose){
            case LYING:
                newpose = new LayPose(player);
                break;
            case SWIMMING:
                newpose = new SwimPose(player);
                break;
            case SITTING:
                newpose = SitPose.getInstance(player);
                break;
            default:
                newpose = new StandingPose();
                break;
        }
        setPose(newpose);
        this.pose.play(null,true);
    }

    public IPluginPose getPose() {
        return pose;
    }

    public EnumPose getPoseType(){
        return getPose().getPose();
    }
}
