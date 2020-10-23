package ru.armagidon.poseplugin.plugin.configuration.settings;

public interface ConfigSetting<T>
{
    ConfigSetting<Boolean> CHECK_FOR_UPDATES = new ConfigSetting<Boolean>() {
        @Override
        public String name() {
            return "check-for-updates";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }
    };
    ConfigSetting<Boolean> EXPERIMENTAL_MODE = new ConfigSetting<Boolean>() {
        @Override
        public String name() {
            return "x-mode";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }
    };
    ConfigSetting<String> LOCALE = new ConfigSetting<String>() {
        @Override
        public String name() {
            return "locale";
        }

        @Override
        public Class<String> getTypeClass() {
            return String.class;
        }
    };
    ConfigSetting<Boolean> STANDUP_WHEN_DAMAGED = new ConfigSetting<Boolean>() {
        @Override
        public String name() {
            return "stand-up-when-damaged";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }
    };

    String name();
    Class<T> getTypeClass();
}
