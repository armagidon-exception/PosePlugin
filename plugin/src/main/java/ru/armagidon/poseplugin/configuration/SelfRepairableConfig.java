package ru.armagidon.poseplugin.configuration;

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
        file = new File(where, name+".yml");
        if(!file.exists()) {
            if(file.createNewFile()) System.out.println("File "+name+".yml successfully created!");
        }
        configuration = YamlConfiguration.loadConfiguration(file);
        fixConfig(generateDefaults(configuration));
    }

    public SelfRepairableConfig(String name) {
        this(PosePlugin.getInstance().getDataFolder(), name);
    }

    @SneakyThrows
    private void fixConfig(YamlConfiguration defaults) {
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
        fixConfig(generateDefaults(configuration));
    }
}
