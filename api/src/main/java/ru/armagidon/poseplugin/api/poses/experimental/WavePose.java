package ru.armagidon.poseplugin.api.poses.experimental;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.poses.EnumPose;

public class WavePose extends ExperimentalPose
{

    public WavePose(Player target) {
        super(target, Material.TRIDENT);
    }

    @Override
    public EnumPose getType() {
        return EnumPose.WAVING;
    }
}
