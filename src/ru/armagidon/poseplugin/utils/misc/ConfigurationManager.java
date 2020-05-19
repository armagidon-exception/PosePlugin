package ru.armagidon.poseplugin.utils.misc;

import org.bukkit.configuration.file.FileConfiguration;
import ru.armagidon.poseplugin.PosePlugin;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager
{
    private static FileConfiguration config = PosePlugin.getPlugin(PosePlugin.class).getConfig();
    private static final Map<String, Object> fields = new HashMap<>();


    //Keys
    public static String STAND_UP = "stand-up-message";
    public static String IN_AIR = "in-air";
    public static String SWIM_ANIMATION_MSG = "swim-animation.message";
    public static String LAY_ANIMATION_MSG = "lay-animation.message";
    public static String SIT_ANIMATION_MSG = "sit-animation.message";
    public static String STAND_UP_WHEN_DAMAGE = "swim-animation.stand-up-when-damage";
    public static String CHECK_FOR_UPDATED = "check-for-updates";
    public static String SWIM_ENABLED = "swim-animation.enabled";
    public static String SIT_WITHOUT_COMMAND = "sit-animation.sit-without-command";


    static  {
       fields();
    }

    public ConfigurationManager() {
        init();
    }


    //Contains all config fields
    private static void fields(){
        fields.put(STAND_UP,"§bYou've stood up");
        fields.put(IN_AIR,"§cYou can't do this in air!");
        fields.put(CHECK_FOR_UPDATED,true);
        fields.put(SWIM_ENABLED,true);
        fields.put(SWIM_ANIMATION_MSG,"§bYou're swimming");
        fields.put(LAY_ANIMATION_MSG,"§bYou've laid down");
        fields.put(SIT_ANIMATION_MSG,"§bYou've sat down");
        fields.put(SIT_WITHOUT_COMMAND,false);
        fields.put(STAND_UP_WHEN_DAMAGE,true);
    }


    private void init(){
        fields.forEach((p,v)->{
            if(config.get(p)==null){
                config.set(p,v);
            }
        });
        PosePlugin.getPlugin(PosePlugin.class).saveConfig();
    }

    public static Object get(String key){
        return config.get(key);
    }
}
