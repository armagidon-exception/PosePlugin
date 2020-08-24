package ru.armagidon.poseplugin.api.poses.experimental.wave;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.experimental.ExperimentalPose;

public class WavePose extends ExperimentalPose
{

    public WavePose(Player target) {
        super(target, PosePluginAPI.getAPI().getNMSFactory().createItemUtil(new ItemStack(Material.TRIDENT)).addTag("PosePluginItem","TRIDENT").getSource());
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.WAVING;
    }
}
