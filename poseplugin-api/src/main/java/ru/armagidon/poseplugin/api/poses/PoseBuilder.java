package ru.armagidon.poseplugin.api.poses;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.utils.versions.PoseAvailabilitySince;
import ru.armagidon.poseplugin.api.utils.versions.VersionControl;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class PoseBuilder {

    private final EnumPoseOption<?>[] defaultOptions;
    private final Map<EnumPoseOption, Object> modifiedOptions = new HashMap<>();
    private final EnumPose pose;

    private PoseBuilder(EnumPose pose) {
        this.pose = pose;
        this.defaultOptions = pose.availableOptions();
    }

    public static PoseBuilder builder(EnumPose pose) {
        return new PoseBuilder(pose);
    }

    public <V> PoseBuilder option(EnumPoseOption<V> option, V value){
        modifiedOptions.put(option, value);
        return this;
    }


    public IPluginPose build(Player player) throws IllegalMCVersionException{

        EnumPose poseType = this.pose;

        IPluginPose pose;
        try {
            pose = poseType.getPoseClass().getDeclaredConstructor(Player.class).newInstance(player);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (pose.getClass().isAnnotationPresent(PoseAvailabilitySince.class)) {
            int currentVersion = VersionControl.getMCVersion();
            PoseAvailabilitySince ann = pose.getClass().getAnnotation(PoseAvailabilitySince.class);
            int allowedVersion = VersionControl.getVersionPriority(ann.version());
            if (currentVersion == -1) {
                throw new IllegalMCVersionException("This version is not supported by plugin");
            }
            if(allowedVersion > currentVersion) {
                throw new IllegalMCVersionException("This pose is for newer versions of minecraft");
            }
        }

        for (EnumPoseOption option : defaultOptions) {
            Object value;
            if(modifiedOptions.containsKey(option))
                value = modifiedOptions.get(option);
            else
                value = option.defaultValue();
            pose.getProperties().getProperty(option.mapper(), option.getTypeClass()).initialize(value);
        }

        return pose;
    }

    public IPluginPose build(PosePluginPlayer player){
        return build(player.getHandle());
    }
}
