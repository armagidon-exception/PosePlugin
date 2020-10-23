package ru.armagidon.poseplugin.plugin.configuration;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.plugin.configuration.settings.ConfigSetting;
import ru.armagidon.poseplugin.plugin.configuration.strategies.ConfigRepairStrategy;
import ru.armagidon.poseplugin.plugin.configuration.strategies.SimpleConfigRepairStrategy;

import java.io.File;

public class ConfigManager
{

    private final File file;
    private @NonNull @Getter FileConfiguration configuration;
    private final ConfigRepairStrategy repairStrategy;


    @SneakyThrows
    private ConfigManager(File where, String name) {
        if(!where.exists()){
            PosePlugin.getInstance().getLogger().info("Creating data folder..." + where.mkdirs());
        }
        file = new File(where, name + ".yml");
        if(!file.exists()) {
            if(file.createNewFile()) PosePlugin.getInstance().getLogger().info("File " + name + ".yml successfully created!");
        }
        this.repairStrategy = new SimpleConfigRepairStrategy();
        configuration = YamlConfiguration.loadConfiguration(file);
        repairStrategy.repair(generateDefaults(configuration),configuration, file);
    }

    public ConfigManager() {
        this(PosePlugin.getInstance().getDataFolder(), "config");
    }

    @SneakyThrows
    protected YamlConfiguration generateDefaults(FileConfiguration configuration) {
        YamlConfiguration defaults = new YamlConfiguration();
        //Add defaults for config
        {
            defaults.set("locale", "en");
            defaults.set("check-for-updates", true);
            defaults.set("x-mode", false);
            {
                ConfigurationSection swim = defaults.createSection("swim");
                swim.set("static", false);
                swim.set("stand-up-when-damaged", true);
                swim.set("enabled", true);
            }
            {
                ConfigurationSection sit = defaults.createSection("sit");
                sit.set("stand-up-when-damaged", true);
            }
            {
                ConfigurationSection lay = defaults.createSection("lay");
                lay.set("stand-up-when-damaged", true);
                lay.set("view-distance", 20);
                lay.set("head-rotation", true);
                lay.set("swing-animation", true);
                lay.set("sync-equipment", true);
                lay.set("sync-overlays", true);
                lay.set("prevent-use-when-invisible", false);
            }
            if (configuration.getBoolean("x-mode")) {
                {
                    ConfigurationSection wave = defaults.createSection("wave");
                    wave.set("enabled", true);
                    wave.set("stand-up-when-damaged", true);
                    wave.set("disable-when-shift",false);
                }
                {
                    ConfigurationSection point = defaults.createSection("point");
                    point.set("enabled", true);
                    point.set("stand-up-when-damaged", true);
                    point.set("disable-when-shift",false);
                }
                {
                    ConfigurationSection handshake = defaults.createSection("handshake");
                    handshake.set("enabled",true);
                    handshake.set("stand-up-when-damaged",true);
                    handshake.set("disable-when-shift",false);
                }
            }
        }
        return defaults;
    }

    @SuppressWarnings("ALL")
    public <T> T get(ConfigCategory category, ConfigSetting<T> setting){
        String path = category.getCategoryName()==null ? "" : category.getCategoryName()+".";
        return (T) getConfiguration().get(path+setting.name());
    }

    public void reload() {
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
        repairStrategy.repair(generateDefaults(configuration),configuration, file);
    }
}
