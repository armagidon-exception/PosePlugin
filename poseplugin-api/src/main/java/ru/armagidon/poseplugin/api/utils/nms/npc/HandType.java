package ru.armagidon.poseplugin.api.utils.nms.npc;

public enum HandType
{
    LEFT(false),
    RIGHT(true);

    private final boolean handModeFlag;

    HandType(boolean handModeFlag) {
        this.handModeFlag = handModeFlag;
    }

    public boolean getHandModeFlag(){
        return handModeFlag;
    }

    public HandType opposite() {
        return switch (this) {
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }
}
