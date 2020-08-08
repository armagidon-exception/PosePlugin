package ru.armagidon.poseplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.command.PluginCommands;
import ru.armagidon.poseplugin.config.ConfigConstants;
import ru.armagidon.poseplugin.config.ConfigManager;
import ru.armagidon.poseplugin.plugin.PluginEventListener;
import ru.armagidon.poseplugin.plugin.UpdateChecker;
import ru.armagidon.poseplugin.plugin.messaging.Messages;

public final class PosePlugin extends JavaPlugin implements Listener
{
    private static PosePlugin instance;
    private final ConfigManager configManager;

    public static PosePlugin getInstance() {
        return instance;
    }

    public static UpdateChecker checker;
    private Messages messages;

    private PluginEventListener listener;


    public PosePlugin() {
        instance = this;
        configManager = new ConfigManager();
        try {
            this.messages = new Messages(ConfigConstants.locale());
        } catch (IllegalArgumentException e){
            setEnabled(false);
            getLogger().severe(e.getMessage());
        }
    }

    @Override
    public void onEnable() {
        PosePluginAPI.getAPI().init(this);
        listener = new PluginEventListener();
        getServer().getPluginManager().registerEvents(listener,this);
        //Init commands
        initCommands();
        //Check for updates
        checkForUpdates();
    }

    @Override
    public void onDisable() {
        PosePluginAPI.getAPI().shutdown();
    }

    private void initCommands(){
        PluginCommands pcs = new PluginCommands();
        pcs.initCommands();
    }

    public Messages message(){
        return messages;
    }

    private void checkForUpdates(){
        if(ConfigConstants.checkForUpdates()){
            checker = new UpdateChecker();
            checker.runTaskAsynchronously(this);
        }
    }

    public PluginEventListener getListener() {
        return listener;
    }

    @Override
    public void reloadConfig() {
        configManager.reload();
    }

    @NotNull
    @Override
    public FileConfiguration getConfig() {
        return configManager.getConfig();
    }
}
