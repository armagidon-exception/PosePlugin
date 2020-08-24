package ru.armagidon.poseplugin.plugin.configuration.messaging;

public enum Message {
    IN_AIR("in-air"),
    LAY_PREVENT_INVISIBILITY("lay.prevent-use-when-invisible"),
    LAY_PREVENT_USE_POTION("lay.prevent-use-invisibility-when-use");

    private final String s;

    Message(String s) {
        this.s = s;
    }

    public String getMessage() {
        return s;
    }
}
