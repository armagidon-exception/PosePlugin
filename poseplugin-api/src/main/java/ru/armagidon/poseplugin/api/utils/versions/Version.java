package ru.armagidon.poseplugin.api.utils.versions;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import ru.armagidon.poseplugin.api.poses.IPluginPose;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

import java.util.Arrays;

public enum Version
{

    v1_15("1.15"),
    v1_16("1.16"),
    v1_17("1.17", true);

    private static final ImmutableMap<String, Version> versions;

    static {
        ImmutableMap.Builder<String, Version> builder = ImmutableMap.builder();
        Arrays.stream(values()).forEach(v -> builder.put(v.versionName, v));
        versions = builder.build();
    }


    private final String versionName;
    @Getter private final boolean forceload;

    Version(String versionName, boolean forceload) {
        this.versionName = versionName;
        this.forceload = forceload;
    }

    Version(String versionName) {
        this(versionName, false);
    }

    /*
     * Gets minecraft version in version priority. The newer version it is, the higher the priority will be.
     */

    public static int getCurrentVersionPriority() {
        return getVersionPriority(getCurrentVersionString());
    }

    public static String getCurrentVersionString() {
        String s = ReflectionTools.nmsVersion().replace("_", ".");
        int index = s.indexOf("R") - 2;
        return s.replace("v", "").substring(0, index).trim();
    }

    public static Version getVersion() {
        return versions.get(getCurrentVersionString());
    }

    public static int getVersionPriority(String version) {
        return versions.get(version) == null ? -1 : versions.get(version).ordinal();
    }

    public static boolean isAvailable(Class<? extends IPluginPose> clazz) {
        if (clazz.isAnnotationPresent(PoseAvailabilitySince.class)) {
            int currentVersion = Version.getCurrentVersionPriority();
            PoseAvailabilitySince ann = clazz.getAnnotation(PoseAvailabilitySince.class);
            int allowedVersion = Version.getVersionPriority(ann.version());
            if (currentVersion == -1)
                return false;
            return allowedVersion <= currentVersion;
        }
        return true;
    }
}
