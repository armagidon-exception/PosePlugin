package ru.armagidon.poseplugin.plugin.configuration;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import ru.armagidon.poseplugin.PosePlugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Messages
{
    private File file;
    private @Getter YamlConfiguration configuration;
    private final File localeFolder;

    public Messages(Plugin plugin, String locale) {
        this.localeFolder = new File(plugin.getDataFolder(), "/locale");
        if (!localeFolder.exists()) localeFolder.mkdirs();
        reload(locale);
    }

    public void reload(String locale){
        this.file = new File(localeFolder, locale + ".yml");
        if (!file.exists()) {
            if (!loadDefaultLocale(locale)){
                if (!loadDefaultLocale("en")) {
                    PosePlugin.getInstance().getLogger().severe("Failed to load message file!");
                } else
                    file = new File(localeFolder, "en.yml");
            }
        }
        configuration = YamlConfiguration.loadConfiguration(file);
    }

    public String getColorized(String path){
        String raw = configuration.getString(path);
        if (raw == null) raw = "";
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public void send(CommandSender sender, String path){
        String raw = configuration.getString(path);
        if (raw == null) return;
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', raw));
    }

    private boolean loadDefaultLocale(String locale){
        try {
            BufferedInputStream fileInput = new BufferedInputStream(Config.class.getResourceAsStream("/locale/" + locale + ".yml"));
            if (fileInput.available() == 0) return false;
            try {
                Files.copy(fileInput, file.toPath());
            } catch (IOException e) {
                PosePlugin.getInstance().getLogger().severe("Failed to create config file!");
            }
            return true;
        } catch (IOException e){
            return false;
        }
    }
}
