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
import ru.armagidon.poseplugin.plugin.commands.PoseCommandGenerator;
import ru.armagidon.poseplugin.plugin.corewrapper.CoreWrapper;
import ru.armagidon.poseplugin.plugin.corewrapper.PaperCoreWrapper;
import ru.armagidon.poseplugin.plugin.corewrapper.SpigotCoreWrapper;
import ru.armagidon.poseplugin.api.utils.misc.BlockPositionUtils;
import ru.armagidon.poseplugin.api.utils.nms.npc.HandType;
import ru.armagidon.poseplugin.api.utils.versions.Version;
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
        if (PaperLib.isPaper()) {
            coreWrapper = new PaperCoreWrapper(this);
        } else {
            coreWrapper = new SpigotCoreWrapper(this);
        }
        PoseCommandGenerator.generatePoseCommands();
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
}
