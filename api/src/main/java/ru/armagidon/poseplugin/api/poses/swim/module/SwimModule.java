package ru.armagidon.poseplugin.api.poses.swim.module;

import lombok.Getter;
import org.bukkit.event.Listener;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.personalListener.PersonalListener;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.swim.SwimPose;
import ru.armagidon.poseplugin.api.ticking.Tickable;

public abstract class SwimModule implements Tickable, Listener, PersonalListener {

    private @Getter final PosePluginPlayer target;

    public SwimModule(PosePluginPlayer target) {
        this.target = target;
        PosePluginAPI.getAPI().registerListener(this);
        PosePluginAPI.getAPI().getPersonalHandlerList().subscribe(target, this);
    }

    public abstract void play();

    public abstract void stop();

    public abstract SwimPose.SwimMode getMode();
}
