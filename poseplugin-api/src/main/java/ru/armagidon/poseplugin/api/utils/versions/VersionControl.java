package ru.armagidon.poseplugin.api.utils.versions;

import com.google.common.collect.ImmutableMap;
import ru.armagidon.poseplugin.api.poses.IPluginPose;
import ru.armagidon.poseplugin.api.poses.IllegalMCVersionException;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

public class VersionControl
{
    private static final ImmutableMap<String, Integer> versionPriorities = ImmutableMap.<String, Integer>builder().
            put("1.15", 1).
            put("1.16", 2).
            put("1.17", 3).build();

    /*
     * Gets minecraft version in version priority. The newer version it is, the higher the priority will be.
     */
    public static int getMCVersion() {
        return getVersionPriority(getMCVersionString());
    }

    public static String getMCVersionString() {
        String s = ReflectionTools.nmsVersion().replace("_", ".");
        int index = s.indexOf("R") - 2;
        return s.replace("v", "").substring(0, index).trim();
    }



    public static int getVersionPriority(String version) {
        return versionPriorities.getOrDefault(version, -1);
    }

    public static boolean isAvailable(Class<? extends IPluginPose> clazz) {
        if (clazz.isAnnotationPresent(PoseAvailabilitySince.class)) {
            int currentVersion = VersionControl.getMCVersion();
            PoseAvailabilitySince ann = clazz.getAnnotation(PoseAvailabilitySince.class);
            int allowedVersion = VersionControl.getVersionPriority(ann.version());
            if (currentVersion == -1)
                return false;
            return allowedVersion <= currentVersion;
        }
        return true;
    }
}
