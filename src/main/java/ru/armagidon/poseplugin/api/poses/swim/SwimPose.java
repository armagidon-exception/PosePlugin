package ru.armagidon.poseplugin.api.poses.swim;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;

public abstract class SwimPose extends PluginPose {

    public SwimPose(Player target) {
        super(target);
        initTickModules();
    }

    public static SwimPose newInstance(Player player) {
        final boolean STATIC = PosePlugin.getInstance().getConfig().getBoolean("swim.static");
        if(STATIC){
            return new StaticSwimPose(player);
        } else {
            return new NonStaticSwimPose(player);
        }
    }

    @Override
    public EnumPose getPose() {
        return EnumPose.SWIMMING;
    }

    @Override
    public String getSectionName() {
        return "swim";
    }
}