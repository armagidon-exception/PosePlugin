package ru.armagidon.poseplugin.api.events;

import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.IPluginPose;

public class PostPoseChangeEvent extends PoseEvent
{

    public PostPoseChangeEvent(PosePluginPlayer player, IPluginPose pose) {
        super(player, pose);
    }

    public EnumPose getPoseType(){
        return getNewPose().getType();
    }
}
