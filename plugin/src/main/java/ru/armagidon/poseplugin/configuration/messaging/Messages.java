package ru.armagidon.poseplugin.configuration.messaging;

import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.configuration.ConfigConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

public class Messages
{
    private FileConfiguration localeConfig;
    private File localeFolder;
    private Function<String, String> COLORIZE = (string)-> ChatColor.translateAlternateColorCodes('&',string);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Messages(String locale) {
        localeFolder = new File(PosePlugin.getInstance().getDataFolder(), "/locale");
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
                        en.toPath());
        }
    }

    public void send(String path, CommandSender sender) {
        sender.sendMessage(COLORIZE.apply(localeConfig.getString(path)));
    }

    @SneakyThrows
    public void reload(){
        localeConfig = YamlConfiguration.loadConfiguration(new File(localeFolder, ConfigConstants.locale()+".yml"));
        save(ConfigConstants.locale(), localeFolder);
    }
}