package ru.armagidon.poseplugin.plugin.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import ru.armagidon.poseplugin.PosePlugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Config
{
    private final File file;
    private FileConfiguration configuration;

    public Config(Plugin plugin) {
        this.file = new File(plugin.getDataFolder(), "config.yml");
        reload();
    }

    public FileConfiguration getCfg() {
        return configuration;
    }

    public void reload(){
        if (!file.exists()){
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            loadFromJar();
        }
        configuration = YamlConfiguration.loadConfiguration(file);
        repair(YamlConfiguration.loadConfiguration(new InputStreamReader(Config.class.getClassLoader().getResourceAsStream("/config.yml"))));
    }

    private void repair(YamlConfiguration defaults) {
        AtomicInteger integer = new AtomicInteger(0);
        Set<String> defaultKeys = defaults.getKeys(true);
        Set<String> presentedKeys = configuration.getKeys(true);
        defaultKeys.forEach(key -> {
            if(!presentedKeys.contains(key)){
                integer.incrementAndGet();
                Object defaultValue = defaults.get(key);
                configuration.set(key, defaultValue);
            }
        });
        if(integer.get() != 0){
            try {
                configuration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadFromJar(){
        BufferedInputStream fileInput = new BufferedInputStream(Config.class.getClassLoader().getResourceAsStream("/config.yml"));
        try {
            Files.copy(fileInput, file.toPath());
        } catch (IOException e) {
            PosePlugin.getInstance().getLogger().severe("Failed to create config file!");
        }
    }

    public int getInt(String path){
        return getCfg().getInt(path);
    }

    public boolean getBoolean(String path){
        return getCfg().getBoolean(path);
    }

    public String getString(String path){
        return getCfg().getString(path);
    }
}
