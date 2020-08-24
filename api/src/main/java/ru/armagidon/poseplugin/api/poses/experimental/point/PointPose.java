package ru.armagidon.poseplugin.api.poses.experimental.point;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.experimental.ExperimentalPose;

public class PointPose extends ExperimentalPose {

    public PointPose(Player target) {
        super(target, PosePluginAPI.getAPI().getNMSFactory().createItemUtil(new ItemStack(Material.BOW)).addTag("PosePluginItem","BOW").getSource());
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.POINTING;
    }
}
