package ru.armagidon.poseplugin.plugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PoseBuilder;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.utils.npc.HandType;
import ru.armagidon.poseplugin.plugin.events.HandTypeChangeEvent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExperimentalPoseCommand extends PosePluginCommand
{

    public ExperimentalPoseCommand(@NotNull String name) {
        super(name);
    }

    @Override
    protected boolean execute(Player player, String label, String[] args) {
        PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(player);
        if(args.length != 1) return false;
        String sub = args[0];

        if (!sub.equalsIgnoreCase("off") && !sub.equalsIgnoreCase("left") && !sub.equalsIgnoreCase("right")) return false;

        PosePlugin.PLAYERS_POSES.put(player, p.getPoseType());

        EnumPose pose;
        if(getCommand().getName().equalsIgnoreCase("wave")){
            pose = EnumPose.WAVING;
        } else if(getCommand().getName().equalsIgnoreCase("point")){
            pose = EnumPose.POINTING;
        } else {
            pose = EnumPose.HANDSHAKING;
        }
        return checkMode(p,pose, sub);
    }

    protected boolean checkMode(PosePluginPlayer player, EnumPose pose, String mode){
        if (player.getPoseType() != pose){
            if (mode.equalsIgnoreCase("off")) return true;

            player.changePose(PoseBuilder.builder(pose).option(EnumPoseOption.MODE, HandType.valueOf(mode.toUpperCase())).build(player));

            return true;
        }

        if (mode.equalsIgnoreCase("off")){
            player.resetCurrentPose(true);
            PosePlugin.PLAYERS_POSES.remove(player.getHandle());
            return true;
        }

        HandType old = player.getPose().getProperty(EnumPoseOption.MODE).getValue();
        HandType newType = HandType.valueOf(mode.toUpperCase());

        if(newType.equals(old)) return true;

        player.getPose().getProperty(EnumPoseOption.MODE).setValue( HandType.valueOf(mode.toUpperCase()) );

        Bukkit.getPluginManager().callEvent(new HandTypeChangeEvent(old, newType, pose, player));

        return true;
    }

    TabCompleter completer = (s, c, l, a) -> Stream.of("left", "right", "off").filter(st -> st.startsWith(a[0])).
            sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());

    @Override
    protected List<String> tabComplete(Player player, String alias, String[] args) {
        return completer.onTabComplete(player, getCommand(), alias, args);
    }
}
