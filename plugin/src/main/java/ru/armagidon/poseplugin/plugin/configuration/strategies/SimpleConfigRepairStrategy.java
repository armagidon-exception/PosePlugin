package ru.armagidon.poseplugin.plugin.configuration.strategies;

import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleConfigRepairStrategy implements ConfigRepairStrategy
{

    @SneakyThrows
    @Override
    public void repair(YamlConfiguration defaults, FileConfiguration configuration, File dest) {

        AtomicInteger integer = new AtomicInteger(0);
        Set<String> defaultKeys = defaults.getKeys(true);
        Set<String> presentedKeys = configuration.getKeys(true);
        defaultKeys.forEach(key->{
            if(!presentedKeys.contains(key)){
                integer.incrementAndGet();
                Object defaultValue = defaults.get(key);
                configuration.set(key, defaultValue);
            }
        });
        if(integer.get()!=0){
            configuration.save(dest);
        }
    }
}
