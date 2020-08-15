package ru.armagidon.poseplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.command.PluginCommands;
import ru.armagidon.poseplugin.configuration.ConfigConstants;
import ru.armagidon.poseplugin.configuration.ConfigManager;
import ru.armagidon.poseplugin.configuration.messaging.Messages;
import ru.armagidon.poseplugin.plugin.PluginEventListener;
import ru.armagidon.poseplugin.plugin.UpdateChecker;

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
    private PluginCommands pcs = new PluginCommands();


    public PosePlugin() {
        instance = this;
        getDataFolder().mkdirs();
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
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPluginShutDown(PluginDisableEvent event){
                PosePluginAPI.getAPI().shutdown();
                pcs.unregisterAll();
            }
        }, this);
        //Init commands
        pcs.initCommands();
        //Check for updates
        checkForUpdates();
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
        return configManager.getConfiguration();
    }
}
