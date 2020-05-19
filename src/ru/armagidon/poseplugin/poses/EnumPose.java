package ru.armagidon.poseplugin.poses;

import static ru.armagidon.poseplugin.utils.misc.ConfigurationManager.*;
public enum EnumPose
{

    STANDING((String)get(STAND_UP)),
    SITTING((String)get(SIT_ANIMATION_MSG)),
    LYING((String)get(LAY_ANIMATION_MSG)),
    SWIMMING((String)get(SWIM_ANIMATION_MSG));

    private String message;

    EnumPose(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
