package ru.armagidon.poseplugin.api.poses;

import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.experimental.HandShakePose;
import ru.armagidon.poseplugin.api.poses.experimental.PointPose;
import ru.armagidon.poseplugin.api.poses.experimental.WavePose;
import ru.armagidon.poseplugin.api.poses.lay.LayPose;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.poses.sit.SitPose;
import ru.armagidon.poseplugin.api.poses.swim.SwimPose;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("ALL")
public class PoseBuilder {


    private static Map<EnumPose, Function<Player, IPluginPose>> POSEBUILDER_REGISTRY = new HashMap<EnumPose, Function<Player, IPluginPose>>(){{
        put(EnumPose.LYING, (player) -> new LayPose(player));
        put(EnumPose.SWIMMING, (player) -> new SwimPose(player));
        put(EnumPose.SITTING, (player) -> new SitPose(player));
        //Exp poses
        put(EnumPose.WAVING, (player) -> new WavePose(player));
        put(EnumPose.POINTING, (player) -> new PointPose(player));
        put(EnumPose.HANDSHAKING, (player) -> new HandShakePose(player));
    }};


    private final EnumPoseOption<?>[] defaultOptions;
    private final Map<EnumPoseOption, Object> modifiedOptions = new HashMap<>();
    private final EnumPose pose;

    private PoseBuilder(EnumPose pose) {
        this.pose = pose;
        this.defaultOptions = pose.availableOptions();
    }

    public static PoseBuilder builder(EnumPose pose) {
        if( !POSEBUILDER_REGISTRY.containsKey(pose) ) throw new IllegalArgumentException("Builder for this pose is not registered!");
        return new PoseBuilder(pose);
    }

    public <V> PoseBuilder option(EnumPoseOption<V> option, V value){
        modifiedOptions.put(option, value);
        return this;
    }

    public IPluginPose build(Player player){

        EnumPose poseType = this.pose;

        IPluginPose pose = getPoseBuilder(poseType).apply(player);


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

    public static Function<Player, IPluginPose> getPoseBuilder(EnumPose poseType){
        return POSEBUILDER_REGISTRY.get(poseType);
    }

    public static void registerPoseBuilder(EnumPose poseType, Function<Player, IPluginPose> builder) throws NullPointerException{
        if(POSEBUILDER_REGISTRY.containsKey(poseType)) throw new IllegalArgumentException("Builder for this pose already registered");
        if( poseType == null) throw new NullPointerException("Pose type cannot be null");
        if( builder == null ) throw new NullPointerException("Builder cannot be null");

        POSEBUILDER_REGISTRY.put(poseType, builder);
    }
}
