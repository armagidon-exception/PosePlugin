package ru.armagidon.poseplugin.plugin.configuration;

import lombok.Getter;

public enum ConfigCategory
{
    MAIN(null),
    LAY("lay"),
    SIT("sit"),
    SWIM("swim"),
    WAVE("wave"),
    POINT("point"),
    HANDSHAKE("handshake");

    private @Getter final String categoryName;

    ConfigCategory(String categoryName) {
        this.categoryName = categoryName;
    }
}
