package ru.armagidon.poseplugin;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.poses.EnumPose;
import ru.armagidon.poseplugin.plugin.UpdateChecker;
import ru.armagidon.poseplugin.plugin.command.PluginCommands;
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
    private PluginCommands pcs;


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
        pcs = new PluginCommands();
        //Init commands
        pcs.initCommands();
        //Check for updates
        checkForUpdates();
    }

    @Override
    public void onDisable() {
        PosePluginAPI.getAPI().shutdown();
        pcs.unregisterAll();
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
