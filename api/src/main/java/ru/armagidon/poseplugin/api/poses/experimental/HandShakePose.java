package ru.armagidon.poseplugin.api.poses.experimental;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.poses.EnumPose;

public class HandShakePose extends ExperimentalPose
{
    public HandShakePose(Player target) {
        super(target, Material.SHIELD);
    }

    @Override
    public EnumPose getType() {
        return EnumPose.HANDSHAKING;
    }
}
