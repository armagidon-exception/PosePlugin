package ru.armagidon.poseplugin;

import io.papermc.lib.PaperLib;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
import ru.armagidon.poseplugin.api.poses.crawl.CrawlPose;
import ru.armagidon.poseplugin.api.poses.experimental.SpinJitsuPose;
import ru.armagidon.poseplugin.plugin.commands.corewrapper.CoreWrapper;
import ru.armagidon.poseplugin.plugin.commands.corewrapper.PaperCoreWrapper;
import ru.armagidon.poseplugin.plugin.commands.corewrapper.SpigotCoreWrapper;
import ru.armagidon.poseplugin.api.utils.misc.BlockPositionUtils;
import ru.armagidon.poseplugin.api.utils.nms.npc.HandType;
import ru.armagidon.poseplugin.api.utils.versions.VersionControl;
import ru.armagidon.poseplugin.plugin.UpdateChecker;
import ru.armagidon.poseplugin.plugin.commands.SimpleCommand;
import ru.armagidon.poseplugin.plugin.configuration.Config;
import ru.armagidon.poseplugin.plugin.configuration.Messages;
import ru.armagidon.poseplugin.plugin.listeners.MessagePrintingHandler;
import ru.armagidon.poseplugin.plugin.listeners.PluginEventListener;

import java.util.HashMap;
import java.util.Map;

public final class PosePlugin extends JavaPlugin implements Listener
{
    private @Getter final Config cfg;
    private @Getter CoreWrapper coreWrapper;
    private @Getter static PosePlugin instance;
    public static Map<Player, EnumPose> PLAYERS_POSES = new HashMap<>();
    public static UpdateChecker checker;
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
            e.printStackTrace();
            setEnabled(false);
        }

        getServer().getPluginManager().registerEvents(new PluginEventListener(),this);
        getServer().getPluginManager().registerEvents(new MessagePrintingHandler(),this);
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
        if(PaperLib.isPaper()) coreWrapper = new PaperCoreWrapper(this);
        else {
            coreWrapper = new SpigotCoreWrapper(this);
            PaperLib.suggestPaper(this);
        }
        SimpleCommand.Executor simpleExecutor = (sender, label, args) -> {
            try {
                IPluginPose pose = null;
                switch (label) {
                    case "sit":
                        if (!performChecks(SitPose.class, sender)) return true;
                        pose = PoseBuilder.builder(EnumPose.SITTING).build(sender);
                        break;
                    case "crawl":
                        if (!performChecks(CrawlPose.class, sender)) return true;
                        pose = PoseBuilder.builder(EnumPose.CRAWLING).build(sender);
                        break;
                    case "lay":
                        if (!performChecks(LayPose.class, sender)) return true;
                        pose = PoseBuilder.builder(EnumPose.LYING)
                        .option(EnumPoseOption.INVISIBLE, sender.hasPotionEffect(PotionEffectType.INVISIBILITY))
                        .option(EnumPoseOption.HEAD_ROTATION, getCfg().getBoolean("lay.head-rotation"))
                        .option(EnumPoseOption.SWING_ANIMATION, getCfg().getBoolean("lay.swing-animation"))
                        .option(EnumPoseOption.SYNC_EQUIPMENT, getCfg().getBoolean("lay.sync-equipment"))
                        .option(EnumPoseOption.VIEW_DISTANCE, getCfg().getInt("lay.view-distance"))
                        .option(EnumPoseOption.SYNC_OVERLAYS, getCfg().getBoolean("lay.sync-overlays"))
                        .build(sender);
                        break;
                    case "pray":
                        if (!performChecks(PrayPose.class, sender)) return true;
                        pose = PoseBuilder.builder(EnumPose.PRAYING)
                                .option(EnumPoseOption.STEP, getCfg().getFloat("pray.step"))
                                .build(sender);
                        break;
                    case "spin":
                        if (!performChecks(SpinJitsuPose.class, sender)) return true;
                        pose = PoseBuilder.builder(EnumPose.SPINJITSU).build(sender);
                }
                PosePluginPlayer pluginInstance = PosePluginAPI.getAPI().getPlayerMap().getPosePluginPlayer(sender);
                PLAYERS_POSES.put(sender, pose.getType());
                pluginInstance.changePose(pose);
            } catch (IllegalArgumentException ignored) {}
            return true;
        };

        SimpleCommand.Executor turnOff = (sender, label, args) -> {
            EnumPose pose = null;
            switch (label) {
                case "wave":
                    pose = EnumPose.WAVING;
                    break;
                case "point":
                    pose = EnumPose.CLAPPING;
                    break;
                case "handshake":
                    pose = EnumPose.HANDSHAKING;
                    break;
            }
            PosePluginPlayer player = PosePluginAPI.getAPI().getPlayer(sender);
            player.stopGivenPose(pose);
            return true;
        };

        SimpleCommand.Executor switchRight = (sender, label, args) -> {
            EnumPose poseType = null;
            switch (label) {
                case "wave":
                    poseType = EnumPose.WAVING;
                    break;
                case "clap":
                    poseType = EnumPose.CLAPPING;
                    break;
                case "point":
                    poseType = EnumPose.POINTING;
                    break;
                case "handshake":
                    poseType = EnumPose.HANDSHAKING;
                    break;
            }
            PosePluginPlayer player = PosePluginAPI.getAPI().getPlayer(sender);
            if (player.getPose().getType().equals(poseType)) {
                HandType currentHand = player.getPose().getProperty(EnumPoseOption.HANDTYPE).getValue();
                if (currentHand.equals(HandType.LEFT)) {
                    player.getPose().setProperty(EnumPoseOption.HANDTYPE, HandType.RIGHT);
                    messages().send(player.getHandle(), label + ".handmode-change");
                    return true;
                }
            } else {
                IPluginPose pose = PoseBuilder.builder(poseType).option(EnumPoseOption.HANDTYPE, HandType.RIGHT).build(sender);
                if (!performChecks(pose.getClass(), sender)) return true;
                PLAYERS_POSES.put(sender, pose.getType());
                player.changePose(pose);
            }
            return true;
        };

        SimpleCommand.Executor switchLeft = (sender, label, args) -> {
            EnumPose poseType = null;
            switch (label) {
                case "wave":
                    poseType = EnumPose.WAVING;
                    break;
                case "clap":
                    poseType = EnumPose.CLAPPING;
                    break;
                case "handshake":
                    poseType = EnumPose.HANDSHAKING;
                    break;
                case "point":
                    poseType = EnumPose.POINTING;
                    break;
            }
            PosePluginPlayer player = PosePluginAPI.getAPI().getPlayer(sender);
            if (player.getPose().getType().equals(poseType)) {
                HandType currentHand = player.getPose().getProperty(EnumPoseOption.HANDTYPE).getValue();
                if (currentHand.equals(HandType.RIGHT)) {
                    player.getPose().setProperty(EnumPoseOption.HANDTYPE, HandType.LEFT);
                    messages().send(player.getHandle(), label + ".handmode-change");
                    return true;
                }
            } else {
                IPluginPose pose = PoseBuilder.builder(poseType).option(EnumPoseOption.HANDTYPE, HandType.LEFT).build(sender);
                if (!performChecks(pose.getClass(), sender)) return true;
                PLAYERS_POSES.put(sender, pose.getType());
                player.changePose(pose);
            }
            return true;
        };

        SimpleCommand.builder("sit")
                .permission("poseplugin.commands.sit")
                .permissionMessage(coreWrapper.getPermissionMessage())
                .usage(messages.getColorized("sit.usage"))
                .executor(simpleExecutor).register();

        SimpleCommand.builder("lay")
                .permission("poseplugin.commands.lay")
                .permissionMessage(coreWrapper.getPermissionMessage())
                .usage(messages.getColorized("lay.usage"))
                .executor(simpleExecutor).register();

        SimpleCommand.builder("crawl")
                .permission("poseplugin.commands.crawl")
                .permissionMessage(coreWrapper.getPermissionMessage())
                .usage(messages.getColorized("crawl.usage"))
                .executor(simpleExecutor)
                .registerIf(label -> cfg.getBoolean(label + ".enabled"));

        if (cfg.getBoolean("x-mode")) {

            SimpleCommand.builder("spin")
                    .permission("poseplugin.commands.spin")
                    .permissionMessage(coreWrapper.getPermissionMessage())
                    .usage(messages.getColorized("spinjitsu.usage"))
                    .executor(simpleExecutor).registerIf(label -> cfg.getBoolean(label + ".enabled"));

            SimpleCommand.builder("pray")
                    .permission("poseplugin.commands.pray")
                    .permissionMessage(coreWrapper.getPermissionMessage())
                    .usage(messages.getColorized("pray.usage"))
                    .executor(simpleExecutor).registerIf(label -> cfg.getBoolean(label + ".enabled"));

            SimpleCommand.builder("wave")
                    .permission("poseplugin.commands.wave")
                    .permissionMessage(coreWrapper.getPermissionMessage())
                    .usage(messages.getColorized("wave.usage"))
                    .subCommand("off", turnOff)
                    .subCommand("right", switchRight)
                    .subCommand("left", switchLeft)
                    .registerIf(label -> cfg.getBoolean(label + ".enabled"));


            SimpleCommand.builder("clap")
                    .permission("poseplugin.commands.clap")
                    .permissionMessage(coreWrapper.getPermissionMessage())
                    .usage(messages.getColorized("clap.usage"))
                    .subCommand("off", turnOff)
                    .subCommand("right", switchRight)
                    .subCommand("left", switchLeft)
                    .registerIf(label -> cfg.getBoolean(label + ".enabled"));

            SimpleCommand.builder("point")
                    .permission("poseplugin.commands.point")
                    .permissionMessage(coreWrapper.getPermissionMessage())
                    .usage(messages.getColorized("point.usage"))
                    .subCommand("off", turnOff)
                    .subCommand("right", switchRight)
                    .subCommand("left", switchLeft)
                    .registerIf(label -> cfg.getBoolean(label + ".enabled"));

            SimpleCommand.builder("handshake")
                    .permission("poseplugin.commands.handshake")
                    .permissionMessage(coreWrapper.getPermissionMessage())
                    .usage(messages.getColorized("handshake.usage"))
                    .subCommand("off", turnOff)
                    .subCommand("right", switchRight)
                    .subCommand("left", switchLeft)
                    .registerIf(label -> cfg.getBoolean(label + ".enabled"));
        }

    }

    private boolean performChecks(Class<? extends IPluginPose> poseClazz, Player player) {
        if (!onGround(player)) {
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
        return !BlockPositionUtils.getBelow(location).getType().isAir() && player.isOnGround();
    }
}
