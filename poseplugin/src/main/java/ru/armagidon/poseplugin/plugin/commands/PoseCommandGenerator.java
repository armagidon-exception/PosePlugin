package ru.armagidon.poseplugin.plugin.commands;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.IPluginPose;
import ru.armagidon.poseplugin.api.poses.PoseBuilder;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.utils.misc.BlockPositionUtils;
import ru.armagidon.poseplugin.api.utils.nms.npc.HandType;
import ru.armagidon.poseplugin.api.utils.versions.Version;

import java.util.Arrays;

public class PoseCommandGenerator
{
    @SuppressWarnings("unchecked")
    public static void generatePoseCommands() {
        Arrays.stream(EnumPose.values()).forEach(poseType -> {
            String poseName = poseType.getName().toLowerCase();
            SimpleCommand.Builder builder = SimpleCommand
                    .builder(poseType.getName().toLowerCase())
                    .permission("poseplugin.commands."+poseName)
                    .permissionMessage(PosePlugin.getInstance().getCoreWrapper().getPermissionMessage());
            boolean hasHandMode = Arrays.asList(poseType.availableOptions()).contains(EnumPoseOption.HANDTYPE);
            if (hasHandMode) {
                builder.usage(PosePlugin.getInstance().messages().getColorized(poseName+".usage"));
                builder.subCommand("off", (sender, label, args) -> {
                    PosePluginPlayer player = PosePluginAPI.getAPI().getPlayer(sender);
                    player.stopGivenPose(poseType);
                    return true;
                });
                Arrays.stream(HandType.values()).forEach(handType -> builder.subCommand(handType.name().toLowerCase(), (sender, label, args) -> {
                    PosePluginPlayer player = PosePluginAPI.getAPI().getPlayer(sender);
                    if (player.getPoseType().equals(poseType)) {
                        HandType currentHandType = player.getPose().getProperty(EnumPoseOption.HANDTYPE).getValue();
                        if (!currentHandType.equals(handType)) {
                            player.getPose().setProperty(EnumPoseOption.HANDTYPE, handType);
                            PosePlugin.getInstance().messages().send(sender,poseName+".handmode-change");
                        }
                    } else {
                        PosePlugin.PLAYERS_POSES.put(sender, poseType);
                        IPluginPose pose  = PoseBuilder.builder(poseType).option(EnumPoseOption.HANDTYPE, handType).build(sender);
                        player.changePose(pose);
                    }
                    return true;
                }));
            } else {
                builder.executor((sender, label, args) -> {
                    if (!performChecks(poseType.getPoseClass(), sender)) return true;
                    PosePluginPlayer player = PosePluginAPI.getAPI().getPlayer(sender);
                    PoseBuilder poseBuilder = PoseBuilder.builder(poseType);
                    Arrays.stream(poseType.availableOptions()).forEach(option -> {
                        Object value = PosePlugin.getInstance().getConfig().get((poseType.getName() + "." + option.mapper()).toLowerCase());
                        if (value == null) value = option.defaultValue();
                        poseBuilder.option((EnumPoseOption<? super Object>) option, value);
                    });
                    PosePlugin.PLAYERS_POSES.put(sender, poseType);
                    player.changePose(poseBuilder.build(sender));
                    return true;
                });
            }
            builder.registerIf(name -> {
                FileConfiguration configuration = PosePlugin.getInstance().getConfig();
                if (poseType.isExperimental()) {
                    return configuration.getBoolean("x-mode") && configuration.getBoolean(poseName + ".enabled");
                }
                return true;
            });
        });
    }
    private static boolean performChecks(Class<? extends IPluginPose> poseClazz, Player player) {
        if (!onGround(player)) {
            PosePlugin.getInstance().messages().send(player, "in-air");
            return false;
        }
        if (!Version.isAvailable(poseClazz)) {
            PosePlugin.getInstance().messages().send(player, "pose-not-support-version");
            return false;
        }
        return true;
    }

    public static boolean onGround(Player player){
        Location location = player.getLocation();
        return !BlockPositionUtils.getBelow(location).getType().isAir() && player.isOnGround();
    }
}
