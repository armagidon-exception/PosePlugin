package ru.armagidon.poseplugin.plugin.configuration.messaging;

import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.plugin.configuration.ConfigConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Set;
import java.util.function.Function;

public class Messages
{
    private FileConfiguration localeConfig;
    private final File localeFolder;
    private File localeFile;
    private final Function<String, String> COLORIZE = (string)-> ChatColor.translateAlternateColorCodes('&',string);

    public Messages(String locale) {
        localeFolder = new File(PosePlugin.getInstance().getDataFolder(), "/locale");
        if(!localeFolder.exists()){
            PosePlugin.getInstance().getLogger().info("Creating locale folder..." + localeFolder.mkdirs());
        }
        localeFile = new File(localeFolder, locale + ".yml");
        if(!localeFile.exists()){
            pullLocaleFile(locale);
            localeFile = new File(localeFolder, "en.yml");
        }
        this.localeConfig = YamlConfiguration.loadConfiguration(localeFile);
        repair();
    }

    public void send(Message message, CommandSender sender) {
        send(message.getMessage(), sender);
    }

    private void pullLocaleFile(String locale) {
        if(!localeFile.exists()) {
            try {
                Files.copy(getClass().getResourceAsStream("/locale/" + locale + ".yml"),
                        localeFile.toPath());
            } catch (IOException e){
                PosePlugin.getInstance().getLogger().severe("Error occurred while coping locale file!");
            }
        }
    }

    public String getMessage(String path){
        return COLORIZE.apply(localeConfig.getString(path));
    }

    public void send(String path, CommandSender sender) {
        sender.sendMessage(getMessage(path));
    }

    @SneakyThrows
    public void reload(){
        pullLocaleFile(ConfigConstants.locale());
        localeConfig = YamlConfiguration.loadConfiguration(new File(localeFolder, ConfigConstants.locale()+".yml"));
        repair();
    }

    @SneakyThrows
    public void repair(){
        YamlConfiguration original = YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getResourceAsStream("/locale/en.yml")));
        Set<String> originalKeys = original.getKeys(true);
        Set<String> currentKeys = localeConfig.getKeys(true);
        int counter = 0;
        for (String key : originalKeys) {
            if(!currentKeys.contains(key)){
                counter++;
                localeConfig.set(key, original.get(key));
            }
        }
        if(counter!=0) localeConfig.save(localeFile);
    }
}
