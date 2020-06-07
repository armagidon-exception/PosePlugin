package ru.armagidon.poseplugin.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.events.PoseChangeEvent;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.IPluginPose;
import ru.armagidon.poseplugin.api.poses.StandingPose;
import ru.armagidon.poseplugin.api.poses.lay.LayPose;
import ru.armagidon.poseplugin.api.poses.personalListener.PersonalEventHandler;
import ru.armagidon.poseplugin.api.poses.personalListener.PersonalListener;
import ru.armagidon.poseplugin.api.poses.sit.SitPose;
import ru.armagidon.poseplugin.api.poses.swim.SwimPose;
import ru.armagidon.poseplugin.utils.misc.messaging.Message;

import java.lang.reflect.Method;

//SitPlugin player
public class PosePluginPlayer {
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

    public void changePose(EnumPose pose) {
        PoseChangeEvent event = new PoseChangeEvent(this.pose.getPose(), pose, this, true);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        if (player.isSleeping())
            player.wakeup(true);
        this.pose.stop(event.isLog());
        IPluginPose newPose;
        switch (event.getAfter()) {
            case LYING: {
                boolean prevent_invisible = PosePlugin.getInstance().getConfig().getBoolean("lay.prevent-use-when-invisible");
                if (prevent_invisible && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    PosePlugin.getInstance().message().send(Message.LAY_PREVENT_INVISIBILITY, getPlayer());
                    return;
                }
                newPose = new LayPose(player);
                break;
            }
            case SWIMMING:
                newPose = new SwimPose(player);
                break;
            case SITTING:
                newPose = new SitPose(player);
                break;
            default:
                newPose = new StandingPose();
                break;
        }
        setPose(newPose);
        this.pose.play(null, event.isLog());
    }

    public IPluginPose getPose() {
        return pose;
    }

    public EnumPose getPoseType() {
        return getPose().getPose();
    }

    public void callPersonalEvent(Event event) {
        try {
            PersonalListener listener = (PersonalListener) getPose();
            forEachMethods(listener, event);
        } catch (ClassCastException ignored) {
        }
    }

    private void forEachMethods(PersonalListener listener, Event event) {
        Class superclass = listener.getClass();
        while (superclass != null) {
            for (Method m : superclass.getDeclaredMethods()) {
                if (!m.isAnnotationPresent(PersonalEventHandler.class)) continue;
                try {
                    m.invoke(listener, event);
                } catch (Exception ignored) {
                }
            }
            superclass = superclass.getSuperclass();
        }
    }
}
