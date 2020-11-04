package ru.armagidon.poseplugin.plugin.command;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.PoseBuilder;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.utils.misc.VectorUtils;
import ru.armagidon.poseplugin.plugin.configuration.ConfigManager;
import ru.armagidon.poseplugin.plugin.configuration.messaging.Message;

import static ru.armagidon.poseplugin.plugin.configuration.ConfigCategory.LAY;
import static ru.armagidon.poseplugin.plugin.configuration.settings.LaySettings.*;

public class SimpleCommands extends PosePluginCommand
{

    public SimpleCommands(@NotNull String name) {
        super(name);
    }

    @Override
    protected boolean execute(Player sender, String label, String[] args) {

        PosePluginPlayer p = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(sender.getName());
        if(!VectorUtils.onGround(p.getHandle())){
            PosePlugin.getInstance().message().send(Message.IN_AIR, sender);
            return true;
        }

        ConfigManager cfg = PosePlugin.getInstance().getConfigManager();

        PosePlugin.PLAYERS_POSES.put(sender, p.getPoseType());
        try {
            if (getCommand().getName().equalsIgnoreCase("swim")) {
                p.changePose(PoseBuilder.builder(EnumPose.SWIMMING).build(sender));

            } else if (getCommand().getName().equalsIgnoreCase("lay")) {
                p.changePose(PoseBuilder.builder(EnumPose.LYING).
                        option(EnumPoseOption.HEAD_ROTATION, cfg.get(LAY, HEAD_ROTATION)).
                        option(EnumPoseOption.SWING_ANIMATION, cfg.get(LAY, SWING_ANIMATION)).
                        option(EnumPoseOption.SYNC_EQUIPMENT, cfg.get(LAY, SYNC_EQUIPMENT)).
                        option(EnumPoseOption.SYNC_OVERLAYS, cfg.get(LAY, SYNC_OVERLAYS)).
                        option(EnumPoseOption.INVISIBLE, sender.hasPotionEffect(PotionEffectType.INVISIBILITY)).
                        option(EnumPoseOption.VIEW_DISTANCE, cfg.get(LAY, VIEW_DISTANCE)).build(sender));
            } else if (getCommand().getName().equalsIgnoreCase("sit")) {
                p.changePose(PoseBuilder.builder(EnumPose.SITTING).build(sender));
            }
        } catch (IllegalArgumentException e){
            p.resetCurrentPose();
        }

        return true;
    }
}
