package ru.armagidon.poseplugin.plugin.configuration;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.armagidon.poseplugin.PosePlugin;

import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SelfRepairableConfig
{

    private final File file;
    private @NonNull @Getter FileConfiguration configuration;

    @SneakyThrows
    public SelfRepairableConfig(File where, String name) {
        if(!where.exists()){
            PosePlugin.getInstance().getLogger().info("Creating data folder..." + where.mkdirs());
        }
        file = new File(where, name+".yml");
        if(!file.exists()) {
            if(file.createNewFile()) PosePlugin.getInstance().getLogger().info("File "+name+".yml successfully created!");
        }
        configuration = YamlConfiguration.loadConfiguration(file);
        repair(generateDefaults(configuration));
    }

    public SelfRepairableConfig(String name) {
        this(PosePlugin.getInstance().getDataFolder(), name);
    }

    @SneakyThrows
    private void repair(YamlConfiguration defaults) {
        AtomicInteger integer = new AtomicInteger(0);
        Set<String> defaultKeys = defaults.getKeys(true);
        Set<String> presentedKeys = configuration.getKeys(true);
        defaultKeys.forEach(key->{
            if(!presentedKeys.contains(key)){
                integer.incrementAndGet();
                Object defaultValue = defaults.get(key);
                configuration.addDefault(key, defaultValue);
                configuration.set(key, defaultValue);
            }
        });
        if(integer.get()!=0) {
            configuration.options().copyDefaults(true);
        }
        configuration.save(file);
    }

    protected abstract YamlConfiguration generateDefaults(FileConfiguration loaded);

    public void reload(){
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
        repair(generateDefaults(configuration));
    }
}
