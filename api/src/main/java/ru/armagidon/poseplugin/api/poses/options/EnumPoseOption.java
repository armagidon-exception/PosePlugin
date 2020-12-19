package ru.armagidon.poseplugin.api.poses.options;

import ru.armagidon.poseplugin.api.utils.npc.HandType;

public interface EnumPoseOption<T> {

    EnumPoseOption<HandType> HANDTYPE = new EnumPoseOption<HandType>() {
        @Override
        public String mapper() {
            return "handtype";
        }

        @Override
        public Class<HandType> getTypeClass() {
            return HandType.class;
        }

        @Override
        public HandType defaultValue() {
            return HandType.RIGHT;
        }
    };

    EnumPoseOption<Boolean> HEAD_ROTATION = new EnumPoseOption<Boolean>() {
        @Override
        public String mapper() {
            return "head-rotation";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }

        @Override
        public Boolean defaultValue() {
            return false;
        }
    };

    EnumPoseOption<Boolean> SWING_ANIMATION = new EnumPoseOption<Boolean>() {
        @Override
        public String mapper() {
            return "swing-animation";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }

        @Override
        public Boolean defaultValue() {
            return false;
        }
    };

    EnumPoseOption<Boolean> SYNC_EQUIPMENT = new EnumPoseOption<Boolean>() {
        @Override
        public String mapper() {
            return "sync-equipment";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }

        @Override
        public Boolean defaultValue() {
            return false;
        }
    };

    EnumPoseOption<Boolean> SYNC_OVERLAYS = new EnumPoseOption<Boolean>() {
        @Override
        public String mapper() {
            return "sync-overlays";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }

        @Override
        public Boolean defaultValue() {
            return false;
        }
    };

    EnumPoseOption<Boolean> INVISIBLE = new EnumPoseOption<Boolean>() {
        @Override
        public String mapper() {
            return "invisible";
        }

        @Override
        public Class<Boolean> getTypeClass() {
            return Boolean.class;
        }

        @Override
        public Boolean defaultValue() {
            return false;
        }
    };

    EnumPoseOption<Integer> VIEW_DISTANCE = new EnumPoseOption<Integer>() {
        @Override
        public String mapper() {
            return "view-distance";
        }

        @Override
        public Class<Integer> getTypeClass() {
            return Integer.class;
        }

        @Override
        public Integer defaultValue() {
            return 20;
        }
    };
    
    String mapper();

    Class<T> getTypeClass();

    T defaultValue();
}
