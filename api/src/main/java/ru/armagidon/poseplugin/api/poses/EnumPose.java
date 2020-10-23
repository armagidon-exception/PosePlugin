package ru.armagidon.poseplugin.api.poses;

import ru.armagidon.poseplugin.api.poses.options.EnumPoseOption;

public interface EnumPose
{

    EnumPose STANDING = new EnumPose() {
        @Override
        public String getName() {
            return "stand";
        }

        @Override
        public EnumPoseOption<?>[] availableOptions() {
            return new EnumPoseOption[0];
        }
    };
    EnumPose SITTING = new EnumPose() {
        @Override
        public String getName() {
            return "sit";
        }

        @Override
        public EnumPoseOption<?>[] availableOptions() {
            return new EnumPoseOption[0];
        }
    };
    EnumPose LYING = new EnumPose() {
        @Override
        public String getName() {
            return "lay";
        }

        @Override
        public EnumPoseOption<?>[] availableOptions() {
            return new EnumPoseOption[]{EnumPoseOption.HEAD_ROTATION,
                    EnumPoseOption.SYNC_EQUIPMENT,
                    EnumPoseOption.SYNC_OVERLAYS,
                    EnumPoseOption.VIEW_DISTANCE,
                    EnumPoseOption.INVISIBLE,
                    EnumPoseOption.SWING_ANIMATION};
        }
    };
    EnumPose SWIMMING = new EnumPose() {
        @Override
        public String getName() {
            return "swim";
        }

        @Override
        public EnumPoseOption<?>[] availableOptions() {
            return new EnumPoseOption[0];
        }
    };
    EnumPose WAVING = new EnumPose() {
        @Override
        public String getName() {
            return "wave";
        }

        @Override
        public EnumPoseOption<?>[] availableOptions() {
            return new EnumPoseOption[]{EnumPoseOption.MODE};
        }
    };
    EnumPose POINTING = new EnumPose() {
        @Override
        public String getName() {
            return "point";
        }

        @Override
        public EnumPoseOption<?>[] availableOptions() {
            return new EnumPoseOption[]{EnumPoseOption.MODE};
        }
    };
    EnumPose HANDSHAKING = new EnumPose() {
        @Override
        public String getName() {
            return "handshake";
        }

        @Override
        public EnumPoseOption<?>[] availableOptions() {
            return new EnumPoseOption[]{EnumPoseOption.MODE};
        }
    };

/*
    STANDING("stand"),
    SITTING("sit"),
    LYING("lay"),
    SWIMMING("swim"),
    WAVING("wave"),
    POINTING("point"),
    HANDSHAKING("handshake");*/

    String getName();
    EnumPoseOption<?>[] availableOptions();
}
