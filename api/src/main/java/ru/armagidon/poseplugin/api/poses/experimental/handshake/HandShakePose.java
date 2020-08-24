package ru.armagidon.poseplugin.api.poses.experimental.handshake;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.experimental.ExperimentalPose;

public class HandShakePose extends ExperimentalPose
{
    public HandShakePose(Player target) {
        super(target, PosePluginAPI.getAPI().getNMSFactory().createItemUtil(new ItemStack(Material.SHIELD)).addTag("PosePluginItem","SHIELD").getSource());
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.HANDSHAKING;
    }
}
