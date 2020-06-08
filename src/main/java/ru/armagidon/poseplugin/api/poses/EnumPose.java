package ru.armagidon.poseplugin.api.poses;

import ru.armagidon.poseplugin.utils.misc.messaging.Message;

public enum EnumPose
{

    STANDING(Message.STAND_UP),
    SITTING(Message.SIT_MSG),
    LYING(Message.LAY_MSG),
    SWIMMING(Message.SWIM_MSG);


    private final Message msg;

    EnumPose(Message msg) {
        this.msg =msg;
    }

    public Message getMessage() {
        return msg;
    }
}
