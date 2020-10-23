package ru.armagidon.poseplugin.plugin.configuration.strategies;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public interface ConfigRepairStrategy
{
    void repair(YamlConfiguration defaults, FileConfiguration configuration, File dest);
}
