package ru.armagidon.poseplugin.api.poses.swim.module;

import org.bukkit.event.Listener;
import ru.armagidon.poseplugin.api.poses.swim.SwimPose;

public interface SwimModule extends Listener
{
    void action();

    void stop();

    SwimPose.SwimMode getMode();
}
