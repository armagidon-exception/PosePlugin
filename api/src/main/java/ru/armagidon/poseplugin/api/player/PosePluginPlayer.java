package ru.armagidon.poseplugin.api.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import ru.armagidon.poseplugin.api.events.PoseChangeEvent;
import ru.armagidon.poseplugin.api.events.PostPoseChangeEvent;
import ru.armagidon.poseplugin.api.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.personalListener.PersonalListener;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.IPluginPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.poses.lay.LayPose;
import ru.armagidon.poseplugin.api.poses.sit.SitPose;
import ru.armagidon.poseplugin.api.poses.swim.SwimPose;

import java.lang.reflect.Method;

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

    public void setPose(IPluginPose newpose) {
        this.pose = newpose;
    }

    public void changePose(EnumPose pose){
        PoseChangeEvent event = new PoseChangeEvent(this.pose.getPose(), pose, this);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) return;
        if(player.isSleeping()) player.wakeup(true);
        this.pose.stop();
        IPluginPose newpose;
        switch (event.getAfter()){
            case LYING: {
                newpose = new LayPose(player);
                break;
            }
            case SWIMMING:
                newpose = new SwimPose(player);
                break;
            case SITTING:
                newpose = new SitPose(player);
                break;
            default:
                newpose = PluginPose.standing;
                break;
        }
        setPose(newpose);
        Bukkit.getPluginManager().callEvent(new PostPoseChangeEvent(this, event.getAfter()));
        this.pose.initiate();
        this.pose.play(null);
    }

    public IPluginPose getPose() {
        return pose;
    }

    public EnumPose getPoseType(){
        return getPose().getPose();
    }

    public void callPersonalEvent(Event event){
        try{
            PersonalListener listener = (PersonalListener) getPose();
            forEachMethods(listener, event);
        }catch (ClassCastException ignored){}
    }

    private void forEachMethods(PersonalListener listener, Event event){
        Class<?> superclass = listener.getClass();
        while (superclass!=null) {
            for (Method m : superclass.getDeclaredMethods()) {
                if (!m.isAnnotationPresent(PersonalEventHandler.class)) continue;
                try {
                    m.invoke(listener, event);
                } catch (Exception ignore) {}
            }
            superclass = superclass.getSuperclass();
        }
    }
}
