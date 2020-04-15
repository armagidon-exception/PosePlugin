package ru.armagidon.sit;

import org.bukkit.entity.Player;
import ru.armagidon.sit.poses.*;

//SitPlugin player
public class SitPluginPlayer
{
    private final Player player;

    private PluginPose pose;

    public SitPluginPlayer(Player player) {
        this.player = player;
        this.pose = new StandingPose(player);
    }

    public Player getPlayer() {
        return player;
    }

    public void setPose(PluginPose newpose) {
        this.pose = newpose;
    }

    public void changePose(EnumPose pose){
        this.pose.stop(true);
        PluginPose newpose;
        switch (pose){
            case LYING:
                newpose = new LayPose(player);
                break;
            case SWIM:
                newpose = new SwimPose(player);
                break;
            case SITTING:
                newpose = new SitPose(player);
                break;
            default:
                newpose = new StandingPose(player);
                break;
        }
        setPose(newpose);
        this.pose.play(null,true);
    }

    public PluginPose getPose() {
        return pose;
    }
}
