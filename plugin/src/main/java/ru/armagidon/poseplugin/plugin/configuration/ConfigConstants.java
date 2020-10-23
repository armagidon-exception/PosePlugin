package ru.armagidon.poseplugin.plugin.configuration;

import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.plugin.configuration.settings.ConfigSetting;
import ru.armagidon.poseplugin.plugin.configuration.settings.SwimSettings;

public class ConfigConstants
{

    public static boolean checkForUpdates(){
        return PosePlugin.getInstance().getConfigManager().get(ConfigCategory.MAIN, ConfigSetting.CHECK_FOR_UPDATES);
    }
    
    public static boolean experimentalMode(){
        return PosePlugin.getInstance().getConfigManager().get(ConfigCategory.MAIN, ConfigSetting.EXPERIMENTAL_MODE);
    }

    public static boolean isWaveEnabled(){
        return experimentalMode()&&PosePlugin.getInstance().getConfig().getBoolean("wave.enabled");
    }

    public static boolean isPointEnabled(){
        return experimentalMode()&&PosePlugin.getInstance().getConfig().getBoolean("point.enabled");
    }

    public static boolean isSwimEnabled(){
        return PosePlugin.getInstance().getConfigManager().get(ConfigCategory.SWIM, SwimSettings.ENABLED);
    }

    public static String locale(){
        return PosePlugin.getInstance().getConfigManager().get(ConfigCategory.MAIN, ConfigSetting.LOCALE);
    }

    public static boolean isHandShakeEnabled() {
        return experimentalMode()&&PosePlugin.getInstance().getConfig().getBoolean("handshake.enabled");
    }

    public static boolean isWaveShiftEnabled(){
        return PosePlugin.getInstance().getConfig().getBoolean("wave.disable-when-shift");
    }

    public static boolean isPointShiftEnabled(){
        return PosePlugin.getInstance().getConfig().getBoolean("point.disable-when-shift");
    }

    public static boolean isHandShakeShiftEnabled(){
        return PosePlugin.getInstance().getConfig().getBoolean("handshake.disable-when-shift");
    }

    public static boolean isHeadRotationEnabled(){
        return PosePlugin.getInstance().getConfig().getBoolean("lay.head-rotation");
    }

    public static boolean isSwingAnimationEnabled(){
        return PosePlugin.getInstance().getConfig().getBoolean("lay.swing-animation");
    }

    public static boolean isSynchronizeEquipmentEnabled(){
        return PosePlugin.getInstance().getConfig().getBoolean("lay.synchronize-equipment");
    }

    public static boolean isSynchronizeOverlaysEnabled(){
        return PosePlugin.getInstance().getConfig().getBoolean("lay.synchronize-overlays");
    }

    public static int viewDistance(){
        return PosePlugin.getInstance().getConfig().getInt("lay.view-distance");
    }
}
