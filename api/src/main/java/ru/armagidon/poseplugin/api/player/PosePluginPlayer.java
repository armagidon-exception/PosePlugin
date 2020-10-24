package ru.armagidon.poseplugin.api.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.events.PoseChangeEvent;
import ru.armagidon.poseplugin.api.events.PostPoseChangeEvent;
import ru.armagidon.poseplugin.api.events.StopPosingEvent;
import ru.armagidon.poseplugin.api.poses.AbstractPose;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.IPluginPose;

//SitPlugin player
public class PosePluginPlayer
{

    private final Player player;

    private IPluginPose pose;

    public PosePluginPlayer(Player player) {
        this.player = player;
        this.pose = AbstractPose.STANDING;
    }

    public Player getHandle(){
        return player;
    }

    public void setPose(IPluginPose newPose) {
        this.pose = newPose;
    }

    public void changePose(IPluginPose pose){

        if(pose == null) return;

        if (pose.getType().equals(this.pose.getType()))
            throw new IllegalArgumentException("This pose type is already in use.");

        //Event calling section
        PoseChangeEvent event = new PoseChangeEvent(this.pose.getType(), pose, this);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        //If player is sleeping then stop sleeping
        if(player.isSleeping()) player.wakeup(true);
        IPluginPose newPose = event.getNewPose();
        //Stop pose
        this.pose.stop();

        this.setPose(newPose);

        this.pose.initiate();
        this.pose.play(null);

        PostPoseChangeEvent postChangeEvent = new PostPoseChangeEvent(this, this.getPose());
        Bukkit.getPluginManager().callEvent(postChangeEvent);
    }


    /**
     * @return Whether the pose-resetting has been done
     */
    public boolean resetCurrentPose(boolean cancellable)
    {
        if ( getPoseType().equals(EnumPose.STANDING) ) return true;
        StopPosingEvent stopEvent = new StopPosingEvent(getPoseType(), this, cancellable);
        Bukkit.getPluginManager().callEvent(stopEvent);
        if ( stopEvent.isCancelled() ) return false;
        getPose().stop();
        return true;
    }

    public IPluginPose getPose() {
        return pose;
    }

    public EnumPose getPoseType(){
        return getPose().getType();
    }
}
