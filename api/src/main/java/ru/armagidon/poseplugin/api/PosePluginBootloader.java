package ru.armagidon.poseplugin.api;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class PosePluginBootloader extends JavaPlugin
{
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
        }
    }
}
