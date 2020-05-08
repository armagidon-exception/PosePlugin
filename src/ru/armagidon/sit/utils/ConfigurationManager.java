package ru.armagidon.sit.utils;

import org.bukkit.configuration.file.FileConfiguration;
import ru.armagidon.sit.SitPlugin;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager
{
    private static FileConfiguration config = SitPlugin.getPlugin(SitPlugin.class).getConfig();
    private final Map<String, Object> fields = new HashMap<>();


    //Keys
    public static String STAND_UP = "stand-up-message";
    public static String IN_AIR = "in-air";
    public static String SWIM_ANIMATION_MSG = "swim-animation.message";
    public static String LAY_ANIMATION_MSG = "lay-animation.message";
    public static String SIT_ANIMATION_MSG = "sit-animation.message";
    public static String BREAK_BACK = "swim-animation.break-back";
    public static String COLLIDABLE = "collidable";
    public static String CHECK_FOR_UPDATED = "check-for-updates";
    public static String SWIM_ENABLED = "swim-animation.enabled";
    public static String SIT_WITHOUT_COMMAND = "sit-animation.sit-without-command";


    public ConfigurationManager() {
        fields();
        init();
    }


    //Contains all config fields
    private void fields(){
        fields.put(STAND_UP,"§bYou've stood up");
        fields.put(IN_AIR,"§cYou can't do this in air!");
        fields.put(COLLIDABLE,false);
        fields.put(CHECK_FOR_UPDATED,true);
        fields.put(SWIM_ENABLED,true);
        fields.put(SWIM_ANIMATION_MSG,"§bYou're swimming");
        fields.put(LAY_ANIMATION_MSG,"§bYou've laid down");
        fields.put(SIT_ANIMATION_MSG,"§bYou've sat down");
        fields.put(SIT_WITHOUT_COMMAND,false);
        fields.put(BREAK_BACK,"§cYou suddenly stood up, because someone tried to break your back!");
    }


    private void init(){
        fields.forEach((p,v)->{
            if(config.get(p)==null){
                config.set(p,v);
            }
        });
        SitPlugin.getPlugin(SitPlugin.class).saveConfig();
    }

    public static Object get(String key){
        return config.get(key);
    }
}
