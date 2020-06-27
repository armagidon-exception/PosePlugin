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

public class Messages
{
    private FileConfiguration localeConfig;
    private Function<String, String> COLORIZE = (string)-> ChatColor.translateAlternateColorCodes('&',string);

    public Messages(String locale) {
        File localeFolder = new File(PosePlugin.getInstance().getDataFolder(), "/locale");
        localeFolder.mkdirs();
        File file = new File(localeFolder, locale + ".yml");
        if(!file.exists()){
            try{
                save(locale, localeFolder);
            } catch (Exception e){
                throw new IllegalArgumentException("Locale file \""+locale+ "\" doesn't exists");
            }
        }
        this.localeConfig = YamlConfiguration.loadConfiguration(file);
    }

    public void send(Message message, CommandSender sender) {
        send(message.getMessage(), sender);
    }

    private void save(String locale, File localeFolder) throws IOException {
        File en = new File(localeFolder, locale+".yml");
        if(!en.exists()){
                Files.copy(getClass().getResourceAsStream("/locale/" +locale+".yml"),
                        Paths.get(en.getPath()));
        }
    }

    public void send(String path, CommandSender sender) {
        sender.sendMessage(COLORIZE.apply(localeConfig.getString(path)));
    }
}
