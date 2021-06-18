package ru.armagidon.poseplugin.api.poses;

import lombok.Getter;
import ru.armagidon.poseplugin.api.poses.crawl.CrawlPose;
import ru.armagidon.poseplugin.api.poses.experimental.ExperimentalHandPose;
import ru.armagidon.poseplugin.api.poses.experimental.PrayPose;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.poses.seatrequiring.LayPose;
import ru.armagidon.poseplugin.api.poses.seatrequiring.SitPose;
import ru.armagidon.poseplugin.api.poses.experimental.SpinJitsuPose;

public enum EnumPose {

    STANDING("stand", AbstractPose.STANDING.getClass()),
    SITTING("sit", SitPose.class),
    LYING("lay", LayPose.class, EnumPoseOption.HEAD_ROTATION,
            EnumPoseOption.SYNC_EQUIPMENT,
            EnumPoseOption.SYNC_OVERLAYS,
            EnumPoseOption.VIEW_DISTANCE,
            EnumPoseOption.INVISIBLE,
            EnumPoseOption.SWING_ANIMATION),
    CRAWLING("crawl", CrawlPose.class),
    WAVING("wave", ExperimentalHandPose.WavePose.class, EnumPoseOption.HANDTYPE),
    POINTING("point", ExperimentalHandPose.PointPose.class, EnumPoseOption.HANDTYPE),
    CLAPPING("clap", ExperimentalHandPose.ClapPose.class, EnumPoseOption.HANDTYPE),
    HANDSHAKING("handshake", ExperimentalHandPose.HandShakePose.class, EnumPoseOption.HANDTYPE),
    PRAYING("pray", PrayPose.class, EnumPoseOption.STEP),
    SPINJITSU("spinjitsu", SpinJitsuPose.class);

    @Getter private final String name;
    private final EnumPoseOption<?>[] enumPoseOptions;
    @Getter private final Class<? extends IPluginPose> poseClass;

    EnumPose(String name, Class<? extends IPluginPose> poseClass, EnumPoseOption<?>... enumPoseOptions) {
        this.name = name;
        this.poseClass = poseClass;
        this.enumPoseOptions = enumPoseOptions;
    }

    EnumPose(String name, Class<? extends IPluginPose> poseClass) {
        this(name, poseClass, new EnumPoseOption[0]);
    }

    public EnumPoseOption<?>[] availableOptions(){
        return enumPoseOptions;
    }
}
