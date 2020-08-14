package ru.armagidon.poseplugin.config;

import ru.armagidon.poseplugin.PosePlugin;

public class ConfigConstants
{
    public static boolean checkForUpdates(){
        return PosePlugin.getInstance().getConfig().getBoolean("check-for-updates");
    }

    public static boolean experimentalMode(){
        return PosePlugin.getInstance().getConfig().getBoolean("x-mode");
    }

    public static boolean isWaveEnabled(){
        return experimentalMode()&&PosePlugin.getInstance().getConfig().getBoolean("wave.enabled");
    }

    public static boolean isPointEnabled(){
        return experimentalMode()&&PosePlugin.getInstance().getConfig().getBoolean("point.enabled");
    }

    public static boolean isSwimEnabled(){
        return PosePlugin.getInstance().getConfig().getBoolean("swim.enabled");
    }

    public static String locale(){
        return PosePlugin.getInstance().getConfig().getString("locale","en");
    }

    public static boolean isReapEnabled() {
        return PosePlugin.getInstance().getConfig().getBoolean("reap.enabled");
    }
}
