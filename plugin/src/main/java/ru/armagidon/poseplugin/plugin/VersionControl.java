package ru.armagidon.poseplugin.plugin;

import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

public enum VersionControl
{


    V1_15_2("v1_15_R1"),
    V1_16_1("v1_16_R1"),
    V1_16_2("v1_16_R2");

    private final String packageVersion;

    VersionControl(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public static boolean checkPackageVersion(VersionControl version){
        return version.getPackageVersion().equalsIgnoreCase(ReflectionTools.nmsVersion());
    }
}
