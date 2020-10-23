package ru.armagidon.poseplugin.plugin.configuration.settings;

public interface SwimSettings<T> extends ConfigSetting<T>
{

    SwimSettings<Boolean> ENABLED = new SwimSettings<Boolean>() {
        @Override
        public String name() {
            return "enabled";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }
    };

    SwimSettings<Boolean> STATIC = new SwimSettings<Boolean>() {
        @Override
        public String name() {
            return "static";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }
    };
}
