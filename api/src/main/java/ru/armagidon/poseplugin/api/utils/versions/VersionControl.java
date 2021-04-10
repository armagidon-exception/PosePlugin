package ru.armagidon.poseplugin.api.utils.versions;

import com.google.common.collect.ImmutableMap;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

public class VersionControl
{
    private static final ImmutableMap<String, Integer> versionPriorities = ImmutableMap.<String, Integer>builder().
            put("1.15", 1).
            put("1.16", 2).
            put("1.17", 3).build();

    public static int getMCVersion() {
        String s = ReflectionTools.nmsVersion().replace("_", ".");
        int index = s.indexOf("R") - 2;
        String version = ReflectionTools.nmsVersion().replace("_", ".").replace("v", "").substring(0, index).trim();
        return getVersionPriority(version);
    }

    public static int getVersionPriority(String version) {
        return versionPriorities.getOrDefault(version, -1);
    }
}
