package ru.armagidon.poseplugin.api.poses.experimental;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.poses.EnumPose;

public class PointPose extends ExperimentalPose {

    public PointPose(Player target) {
        super(target, Material.BOW);
    }

    @Override
    public EnumPose getType() {
        return EnumPose.POINTING;
    }
}
