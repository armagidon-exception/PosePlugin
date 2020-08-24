package ru.armagidon.poseplugin.api.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.events.PoseChangeEvent;
import ru.armagidon.poseplugin.api.events.PostPoseChangeEvent;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.IPluginPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.poses.experimental.handshake.HandShakePose;
import ru.armagidon.poseplugin.api.poses.experimental.point.PointPose;
import ru.armagidon.poseplugin.api.poses.experimental.wave.WavePose;
import ru.armagidon.poseplugin.api.poses.lay.LayPose;
import ru.armagidon.poseplugin.api.poses.sit.SitPose;
import ru.armagidon.poseplugin.api.poses.swim.SwimPose;

//SitPlugin player
public class PosePluginPlayer
{

    private final Player player;

    private IPluginPose pose;

    public PosePluginPlayer(Player player) {
        this.player = player;
        this.pose = PluginPose.standing;
    }

    public Player getHandle(){
        return player;
    }

    //Changed name
    @Deprecated
    public Player getPlayer() {
        return player;
    }

    public void setPose(IPluginPose newPose) {
        this.pose = newPose;
    }

    public void changePose(EnumPose pose, boolean apiMode){
        if(apiMode) this.pose.setAPIMode(true);
        PoseChangeEvent event = new PoseChangeEvent(this.pose.getPose(), pose, this);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        if(player.isSleeping()) player.wakeup(true);
        this.pose.stop();
        IPluginPose newPose;
        switch (event.getAfter()) {
            case LYING:
                newPose = new LayPose(player);
                break;
            case SWIMMING:
                newPose = new SwimPose(player);
                break;
            case SITTING:
                newPose = new SitPose(player);
                break;
            case WAVING:
                newPose = new WavePose(player);
                break;
            case POINTING:
                newPose = new PointPose(player);
                break;
            case HANDSHAKING:
                newPose = new HandShakePose(player);
                break;
            default:
                newPose = PluginPose.standing;
                break;
        }
        newPose.setAPIMode(apiMode);
        setPose(newPose);
        Bukkit.getPluginManager().callEvent(new PostPoseChangeEvent(this, event.getAfter()));
        this.pose.initiate();
        this.pose.play(null);
    }

    public void changePose(EnumPose pose){
        changePose(pose, false);
    }

    public IPluginPose getPose() {
        return pose;
    }

    public EnumPose getPoseType(){
        return getPose().getPose();
    }
}
