package ru.armagidon.poseplugin;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.player.PosePluginPlayer;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.api.poses.IPluginPose;
import ru.armagidon.poseplugin.api.poses.PoseBuilder;
import ru.armagidon.poseplugin.api.poses.experimental.PrayPose;
import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;
import ru.armagidon.poseplugin.api.poses.seatrequiring.LayPose;
import ru.armagidon.poseplugin.api.poses.seatrequiring.SitPose;
import ru.armagidon.poseplugin.api.poses.swim.CrawlPose;
import ru.armagidon.poseplugin.api.utils.misc.BlockPositionUtils;
import ru.armagidon.poseplugin.api.utils.versions.VersionControl;
import ru.armagidon.poseplugin.plugin.UpdateChecker;
import ru.armagidon.poseplugin.plugin.commands.SimpleCommand;
import ru.armagidon.poseplugin.plugin.configuration.Config;
import ru.armagidon.poseplugin.plugin.configuration.Messages;
import ru.armagidon.poseplugin.plugin.listeners.MessagePrintingHandler;
import ru.armagidon.poseplugin.plugin.listeners.PluginEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class PosePlugin extends JavaPlugin implements Listener
{
    private @Getter static PosePlugin instance;
    public static Map<Player, EnumPose> PLAYERS_POSES = new HashMap<>();
    public static UpdateChecker checker;
    private @Getter final Config cfg;
    private final Messages messages;


    public PosePlugin() {
        instance = this;
        cfg = new Config(this);
        messages = new Messages(this, cfg.getString("locale").trim().toLowerCase());
    }

    @Override
    public void onEnable() {
        try {
            getLogger().info("Initializing api...");
            PosePluginAPI.initialize(this);
            getLogger().info("API initialized!");
        } catch (Exception e){
            getLogger().severe("Error occurred while initializing API.");
            getLogger().severe(e.getMessage());
            getLogger().severe(Arrays.toString(e.getStackTrace()));
            setEnabled(false);
        }

        getServer().getPluginManager().registerEvents(new PluginEventListener(),this);
        getServer().getPluginManager().registerEvents(new MessagePrintingHandler(),this);
        //TODO init commands
        initCommands();
        //Check for updates
        checkForUpdates();
    }

    @Override
    public void onDisable() {
        PosePluginAPI.getAPI().shutdown();
    }

    private void checkForUpdates(){
        if ( cfg.getBoolean("check-for-updates") ) {
            checker = new UpdateChecker();
            checker.runTaskAsynchronously(this);
        }
    }

    public Messages messages(){
        return messages;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
    }

    private void initCommands() {

        SimpleCommand.Executor simpleExecutor = (player, label, args) -> {
            try {
                IPluginPose pose = null;
                switch (label) {
                    case "sit":
                        if (!performChecks(SitPose.class, player)) return true;
                        pose = PoseBuilder.builder(EnumPose.SITTING).build(player);
                        break;
                    case "crawl":
                        if (!performChecks(CrawlPose.class, player)) return true;
                        pose = PoseBuilder.builder(EnumPose.CRAWLING).build(player);
                        break;
                    case "lay":
                        if (!performChecks(LayPose.class, player)) return true;
                        pose = PoseBuilder.builder(EnumPose.LYING)
                        .option(EnumPoseOption.INVISIBLE, player.hasPotionEffect(PotionEffectType.INVISIBILITY))
                        .option(EnumPoseOption.HEAD_ROTATION, getCfg().getBoolean("lay.head-rotation"))
                        .option(EnumPoseOption.SWING_ANIMATION, getCfg().getBoolean("lay.swing-animation"))
                        .option(EnumPoseOption.SYNC_EQUIPMENT, getCfg().getBoolean("lay.sync-equipment"))
                        .option(EnumPoseOption.VIEW_DISTANCE, getCfg().getInt("lay.view-distance"))
                        .option(EnumPoseOption.SYNC_OVERLAYS, getCfg().getBoolean("lay.sync-overlays"))
                        .build(player);
                        break;
                    case "pray":
                        if (!performChecks(PrayPose.class, player)) return true;
                        pose = PoseBuilder.builder(EnumPose.PRAYING)
                                .option(EnumPoseOption.STEP, getCfg().getFloat("pray.step"))
                                .build(player);
                        break;


                }
                PosePluginPlayer pluginInstance = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(player);
                pluginInstance.changePose(pose);
            } catch (IllegalArgumentException ignored) {}
            return true;
        };

        SimpleCommand.builder("sit")
                .permission("poseplugin.commands.sit")
                .permissionMessage(Bukkit.getPermissionMessage())
                .usage(messages.getColorized("sit.usage"))
                .executor(simpleExecutor).register();

        SimpleCommand.builder("lay")
                .permission("poseplugin.commands.lay")
                .permissionMessage(Bukkit.getPermissionMessage())
                .usage(messages.getColorized("lay.usage"))
                .executor(simpleExecutor).register();

        SimpleCommand.builder("crawl")
                .permission("poseplugin.commands.crawl")
                .permissionMessage(Bukkit.getPermissionMessage())
                .usage(messages.getColorized("crawl.usage"))
                .executor(simpleExecutor).register();
        SimpleCommand.builder("pray")
                .permission("poseplugin.commands.pray")
                .permissionMessage(Bukkit.getPermissionMessage())
                .usage(messages.getColorized("pray.usage"))
                .executor(simpleExecutor).register();


    }

    private boolean performChecks(Class<? extends IPluginPose> poseClazz, Player player) {
        if (onGround(player)) {
            messages().send(player, "in-air");
            return false;
        }
        if (!VersionControl.isAvailable(poseClazz)) {
            messages().send(player, "pose-not-support-version");
            return false;
        }
        return true;
    }

    public static boolean onGround(Player player){
        Location location = player.getLocation();
        return !BlockPositionUtils.getBelow(location).getType().isAir();
    }
}
