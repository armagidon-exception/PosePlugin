package ru.armagidon.poseplugin.api.utils.misc;

import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;

public enum VersionControl
{
    v1_16_2(),
    v1_16_1(),
    v1_15_2();

    public static VersionControl getCurrentVersion() throws IllegalStateException{
        switch (ReflectionTools.nmsVersion()){
            case "v1_16_R2": return v1_16_2;
            case "v1_16_R1": return v1_16_1;
            case "v1_15_R1": return v1_15_2;
            default: throw new IllegalStateException("This version of NMS is not supported!");
        }
    }
}
