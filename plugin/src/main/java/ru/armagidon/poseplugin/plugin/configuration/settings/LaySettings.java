package ru.armagidon.poseplugin.plugin.configuration.settings;

public interface LaySettings<T> extends ConfigSetting<T>
{
    LaySettings<Integer> VIEW_DISTANCE = new LaySettings<Integer>() {
        @Override
        public String name() {
            return "view-distance";
        }

        @Override
        public Class<Integer> getTypeClass() {
            return Integer.class;
        }
    };
    LaySettings<Boolean> HEAD_ROTATION = new LaySettings<Boolean>() {
        @Override
        public String name() {
            return "head-rotation";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }
    };
    LaySettings<Boolean> SYNC_EQUIPMENT = new LaySettings<Boolean>() {
        @Override
        public String name() {
            return "sync-equipment";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }
    };
    LaySettings<Boolean> SYNC_OVERLAYS = new LaySettings<Boolean>() {
        @Override
        public String name() {
            return "sync-overlays";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }
    };
    LaySettings<Boolean> SWING_ANIMATION = new LaySettings<Boolean>() {
        @Override
        public String name() {
            return "swing-animation";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }
    };
    LaySettings<Boolean> PREVENT_USE_WHEN_INVISIBLE = new LaySettings<Boolean>() {
        @Override
        public String name() {
            return "prevent-use-when-invisible";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }
    };

}
