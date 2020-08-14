package ru.armagidon.poseplugin.config;

import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.armagidon.poseplugin.PosePlugin;

import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfigManager 
{

    private final File configFile;
    private FileConfiguration config;

    @SneakyThrows
    public ConfigManager() {
        configFile = new File(PosePlugin.getInstance().getDataFolder(), "config.yml");
        if(!configFile.exists()) configFile.createNewFile();
        config = YamlConfiguration.loadConfiguration(configFile);
        fixConfig();
    }

    @SneakyThrows
    private void fixConfig() {
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
                lay.set("update-equipment", true);
                lay.set("update-overlays", true);
                lay.set("prevent-use-when-invisible", false);
            }
            if (config.getBoolean("x-mode")) {
                {
                    ConfigurationSection wave = defaults.createSection("wave");
                    wave.set("enabled", true);
                    wave.set("stand-up-when-damaged", true);
                }
                {
                    ConfigurationSection point = defaults.createSection("point");
                    point.set("enabled", true);
                    point.set("stand-up-when-damaged", true);
                }
            }
        }
        AtomicInteger integer = new AtomicInteger(0);
        Set<String> defaultKeys = defaults.getKeys(true);
        Set<String> presentedKeys = config.getKeys(true);
        defaultKeys.forEach(key->{
            if(!presentedKeys.contains(key)){
                integer.incrementAndGet();
                Object defaultValue = defaults.get(key);
                config.addDefault(key, defaultValue);
                config.set(key, defaultValue);
            }
        });
        if(integer.get()!=0) {
            config.options().copyDefaults(true);
        }
        config.save(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        fixConfig();
    }
}
