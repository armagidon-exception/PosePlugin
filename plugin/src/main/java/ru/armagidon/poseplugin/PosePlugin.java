package ru.armagidon.poseplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.misc.Debugger;
import ru.armagidon.poseplugin.plugin.PluginEventListener;
import ru.armagidon.poseplugin.plugin.UpdateChecker;
import ru.armagidon.poseplugin.plugin.messaging.Messages;

import java.util.ArrayList;

public final class PosePlugin extends JavaPlugin implements Listener
{
    private static PosePlugin instance;

    public static PosePlugin getInstance() {
        return instance;
    }

    public static UpdateChecker checker;
    private Messages messages;
    private final FileConfiguration config;

    private final Debugger debugger;

    private PluginEventListener listener;

    public PosePlugin() {
        instance = this;
        this.debugger = new Debugger();
        this.config = getConfig();
        try {
            this.messages = new Messages(config.getString("locale", "en"));
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
        //Save config
        saveDefaultConfig();
        //Check for updates
        checkForUpdates();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(command.getName().equalsIgnoreCase("ppreload")){
            Bukkit.getPluginManager().disablePlugin(this);
            reloadConfig();
            Bukkit.getPluginManager().enablePlugin(this);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&8&l[&b&l&nPosePlugin&8&l]&a Plugin reloaded!"));
            return true;
        }
        return true;
    }

    @Override
    public void onDisable() {
        PosePluginAPI.getAPI().shutdown();
    }

    private void initCommands(){
        TabCompleter c = (commandSender, command, s, strings) -> new ArrayList<>();
        PluginCommand sit =getCommand("sit");
        PluginCommand lay =getCommand("lay");
        PluginCommand swim =getCommand("swim");
        PluginCommand ppreload = getCommand("ppreload");
        if(ppreload!=null){
            ppreload.setExecutor(this);
            ppreload.setTabCompleter(c);
        }
        PluginCommands pcs = new PluginCommands();
        if(sit!=null) {
            sit.setExecutor(pcs);
            sit.setTabCompleter(c);
        }
        if(lay!=null) {
            lay.setExecutor(pcs);
            lay.setTabCompleter(c);
        }
        if(swim!=null) {
            swim.setExecutor(pcs);
            swim.setTabCompleter(c);
        }
    }

    public Debugger getDebugger() {
        return debugger;
    }

    public Messages message(){
        return messages;
    }

    private void checkForUpdates(){
        if(config.getBoolean("check-for-updates")){
            checker = new UpdateChecker();
            checker.runTaskAsynchronously(this);
        }
    }

    public PluginEventListener getListener() {
        return listener;
    }
}
