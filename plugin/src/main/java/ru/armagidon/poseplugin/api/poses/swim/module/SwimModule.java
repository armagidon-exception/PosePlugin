package ru.armagidon.poseplugin.api.poses.swim.module;

import org.bukkit.event.Listener;

public interface SwimModule extends Listener
{
    void action();

    void stop();

    SwimModuleType getType();

    enum SwimModuleType{
       FLY,BLOCK_AIR, NONSOLID, WATER, NONE
    }
}
