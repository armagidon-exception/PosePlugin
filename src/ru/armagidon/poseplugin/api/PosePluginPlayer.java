package ru.armagidon.poseplugin.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import ru.armagidon.poseplugin.api.events.PoseChangeEvent;
import ru.armagidon.poseplugin.api.poses.*;
import ru.armagidon.poseplugin.api.poses.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.personalListener.PersonalListener;
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
        this.pose = new StandingPose();
    }

    public Player getPlayer() {
        return player;
    }

    public void setPose(IPluginPose newpose) {
        this.pose = newpose;
    }

    public void changePose(EnumPose pose){
        PoseChangeEvent event = new PoseChangeEvent(this.pose.getPose(), pose, this, true);
        if(event.isCancelled()) return;
        this.pose.stop(event.isLog());
        IPluginPose newpose;
        switch (event.getAfter()){
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
        this.pose.play(null,event.isLog());
    }

    public IPluginPose getPose() {
        return pose;
    }

    public EnumPose getPoseType(){
        return getPose().getPose();
    }

    public void callPersonalEvent(Event event){
        try{
            PersonalListener listener = (PluginPose) getPose();
            Method[] ms = listener.getClass().getDeclaredMethods();
            for (Method m:ms) {
                if(!m.isAnnotationPresent(PersonalEventHandler.class)) continue;
                try {
                    m.invoke(listener, event);
                } catch (Exception e) {}
            }
        }catch (ClassCastException e){ return;}
    }
}
