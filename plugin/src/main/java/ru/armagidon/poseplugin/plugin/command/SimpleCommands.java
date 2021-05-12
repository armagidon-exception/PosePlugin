package ru.armagidon.poseplugin.plugin.command;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.IllegalMCVersionException;
import ru.armagidon.poseplugin.api.poses.PoseBuilder;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.utils.misc.BlockPositionUtils;

public class SimpleCommands extends PosePluginCommand
{

    public SimpleCommands(@NotNull String name) {
        super(name);
    }

    @Override
    protected boolean execute(Player sender, String label, String[] args) {

        PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(sender.getName());
        if(!BlockPositionUtils.onGround(p.getHandle())){
            PosePlugin.getInstance().messages().send(sender, "in-air");
            return true;
        }

        FileConfiguration cfg = PosePlugin.getInstance().getCfg().getCfg();

        PosePlugin.PLAYERS_POSES.put(sender, p.getPoseType());
        try {
            if (getCommand().getName().equalsIgnoreCase("crawl")) {
                p.changePose(PoseBuilder.builder(EnumPose.CRAWLING).build(sender));
            } else if (getCommand().getName().equalsIgnoreCase("lay")) {
                p.changePose(PoseBuilder.builder(EnumPose.LYING).
                        option(EnumPoseOption.HEAD_ROTATION, cfg.getBoolean("lay.head-rotation")).
                        option(EnumPoseOption.SWING_ANIMATION, cfg.getBoolean("lay.swing-animation")).
                        option(EnumPoseOption.SYNC_EQUIPMENT, cfg.getBoolean("lay.update-equipment")).
                        option(EnumPoseOption.SYNC_OVERLAYS, cfg.getBoolean("lay.update-overlays")).
                        option(EnumPoseOption.INVISIBLE, sender.hasPotionEffect(PotionEffectType.INVISIBILITY)).
                        option(EnumPoseOption.VIEW_DISTANCE, cfg.getInt("lay.view-distance")).build(sender));
            } else if (getCommand().getName().equalsIgnoreCase("sit")) {
                p.changePose(PoseBuilder.builder(EnumPose.SITTING).build(sender));
            } else if (getCommand().getName().equalsIgnoreCase("pray")) {
                p.changePose(PoseBuilder.builder(EnumPose.PRAYING).option(EnumPoseOption.STEP, (float) cfg.getDouble("pray.step")).build(sender));
            }
        } catch (IllegalMCVersionException | IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            p.resetCurrentPose();
            e.printStackTrace();
        }

        return true;
    }
}
