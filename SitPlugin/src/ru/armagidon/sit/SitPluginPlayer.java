package ru.armagidon.sit;

import org.bukkit.entity.Player;
import ru.armagidon.sit.poses.*;
import ru.armagidon.sit.poses.sit.SitPose;

//SitPlugin player
public class SitPluginPlayer
{
    private final Player player;

    private IPluginPose pose;

    public SitPluginPlayer(Player player) {
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
            case SWIM:
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
