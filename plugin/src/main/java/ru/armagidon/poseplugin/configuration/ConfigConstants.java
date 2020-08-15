package ru.armagidon.poseplugin.configuration;

import ru.armagidon.poseplugin.PosePlugin;
import ru.armagidon.poseplugin.plugin.VersionControl;

public class ConfigConstants
{
    public static boolean checkForUpdates(){
        return PosePlugin.getInstance().getConfig().getBoolean("check-for-updates");
    }

    public static boolean experimentalMode(){
        return PosePlugin.getInstance().getConfig().getBoolean("x-mode")&&!VersionControl.checkPackageVersion(VersionControl.V1_16_2);
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
}
