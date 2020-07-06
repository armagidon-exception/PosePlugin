package ru.armagidon.poseplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.events.StopAnimationEvent;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PluginPose;
import ru.armagidon.poseplugin.api.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.plugin.messaging.Message;

public class PluginCommands implements CommandExecutor
{

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(sender instanceof Player) {
            PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(sender.getName());
            EnumPose pose;
            if (command.getName().equalsIgnoreCase("sit"))
                pose = EnumPose.SITTING;
            else if (command.getName().equalsIgnoreCase("lay"))
                pose = EnumPose.LYING;
            else if(command.getName().equalsIgnoreCase("swim")){
                if(isSwimEnabled())
                    pose = EnumPose.SWIMMING;
                else {
                    PosePlugin.getInstance().message().send(Message.ANIMATION_DISABLED, sender);
                    return true;
                }
            } else return true;
            if(p.getPoseType().equals(pose)){
                PluginPose.callStopEvent(p.getPoseType(), p, StopAnimationEvent.StopCause.STOPPED);
                return true;
            }
            if(!VectorUtils.onGround(p.getHandle())){
                PosePlugin.getInstance().message().send(Message.IN_AIR, sender);
                return true;
            }
            p.changePose(pose);
        }
        return true;
    }

    private boolean isSwimEnabled(){
        return PosePlugin.getInstance().getConfig().getBoolean("swim.enabled");
    }
}
