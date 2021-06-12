package ru.armagidon.poseplugin.api.utils.nms.npc;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class NPCSynchronizer<T>
{

    protected final FakePlayer<T> fakePlayer;

    public abstract void syncHeadRotation();

    public abstract void syncOverlays();

    public abstract void syncEquipment();
}
