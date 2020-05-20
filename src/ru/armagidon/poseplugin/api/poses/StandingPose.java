package ru.armagidon.poseplugin.api.poses;

import org.bukkit.entity.Player;

public class StandingPose implements IPluginPose
{

    public StandingPose() {}

    @Override
    public void play(Player receiver, boolean log) {}

    @Override
    public void stop(boolean log) {}

    @Override
    public EnumPose getPose() {
        return EnumPose.STANDING;
    }
}
