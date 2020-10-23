package ru.armagidon.poseplugin.plugin.configuration.settings;

public interface ExSettings<T> extends ConfigSetting<T>
{
    ExSettings<Boolean> ENABLED = new ExSettings<Boolean>() {
        @Override
        public String name() {
            return "enabled";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }
    };

    ExSettings<Boolean> DISABLE_WHEN_SHIFT = new ExSettings<Boolean>() {
        @Override
        public String name() {
            return "disable-when-shift";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }
    };
}
