package ru.armagidon.poseplugin.api.poses;

import org.bukkit.entity.Player;

public interface IPluginPose
{
    void play(Player receiver, boolean log);
    void stop(boolean log);
    EnumPose getPose();
}
