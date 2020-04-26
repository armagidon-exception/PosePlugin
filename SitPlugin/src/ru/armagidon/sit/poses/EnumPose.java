package ru.armagidon.sit.poses;

import static ru.armagidon.sit.utils.Utils.configuration;

public enum EnumPose
{

    STANDING(configuration.getString("stand-up-message","stand-up-message")),
    SITTING(configuration.getString("sit-animation.message","sit-animation.message")),
    LYING(configuration.getString("lay-animation.message","lay-animation.message")),
    SWIM(configuration.getString("swim-animation.message","swim-animation.message"));

    private String message;

    EnumPose(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
