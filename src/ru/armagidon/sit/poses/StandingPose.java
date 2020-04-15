package ru.armagidon.sit.poses;

import org.bukkit.entity.Player;

public class StandingPose extends PluginPose
{

    public StandingPose(Player player) {super(player);}

    @Override
    public void play(Player receiver, boolean log) {

    }

    @Override
    public void stop(boolean log) {}

    @Override
    public EnumPose getPose() {
        return EnumPose.STANDING;
    }
}
