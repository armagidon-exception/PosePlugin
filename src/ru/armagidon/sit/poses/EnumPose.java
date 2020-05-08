package ru.armagidon.sit.poses;

import static ru.armagidon.sit.utils.ConfigurationManager.*;
public enum EnumPose
{

    STANDING((String)get(STAND_UP)),
    SITTING((String)get(SIT_ANIMATION_MSG)),
    LYING((String)get(LAY_ANIMATION_MSG)),
    SWIM((String)get(SWIM_ANIMATION_MSG));

    private String message;

    EnumPose(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
