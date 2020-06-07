package ru.armagidon.poseplugin.utils.misc.messaging;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.armagidon.poseplugin.PosePlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

public class Messages {
    private final FileConfiguration locale;
    private final Function<String, String> COLORIZE = (string) -> ChatColor.translateAlternateColorCodes('&', string);

    public Messages(String locale) {
        File localeFolder = new File(PosePlugin.getInstance().getDataFolder(), "locale");
        if (!localeFolder.mkdirs())
            throw new IllegalArgumentException("No directories created");
        saveEn(localeFolder);
        File file = new File(localeFolder, locale + ".yml");
        if (!file.exists())
            throw new IllegalArgumentException("Locale file \"" + locale + "\" doesn't exists");
        this.locale = YamlConfiguration.loadConfiguration(file);
    }

    public void send(Message message, CommandSender sender) {
        send(message.getMessage(), sender);
    }

    private void saveEn(File localeFolder) {
        File en = new File(localeFolder, "en.yml");
        if (en.exists())
            return;
        try {
            Files.copy(getClass().getResourceAsStream("/locale/en.yml"), Paths.get(en.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String path, CommandSender sender) {
        sender.sendMessage(COLORIZE.apply(locale.getString(path)));
    }
}
