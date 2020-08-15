package ru.armagidon.poseplugin.configuration.messaging;

public enum Message {
    IN_AIR("in-air"),
    STAND_UP("stand-up"),
    SWIM_MSG("swim.play"),
    LAY_MSG("lay.play"),
    SIT_MSG("sit.play"),
    LAY_PREVENT_INVISIBILITY("lay.prevent-use-when-invisible"),
    LAY_PREVENT_USE_POTION("lay.prevent-use-invisibility-when-use"),
    ANIMATION_DISABLED("animation-disabled");

    private final String s;

    Message(String s) {
        this.s = s;
    }

    public String getMessage() {
        return s;
    }
}
