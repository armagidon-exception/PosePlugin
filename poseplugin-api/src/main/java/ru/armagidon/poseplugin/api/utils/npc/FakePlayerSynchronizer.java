package ru.armagidon.poseplugin.api.utils.npc;

import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;
import ru.armagidon.poseplugin.api.utils.npc.protocolized.New.NewFakePlayerSynchronizer;
import ru.armagidon.poseplugin.api.utils.npc.protocolized.old.OldFakePlayerSynchronizer;

public interface FakePlayerSynchronizer
{
    void syncHeadRotation();

    void syncOverlays();

    void syncEquipment();

    static FakePlayerSynchronizer createNew(FakePlayer npc) {
        if (ReflectionTools.nmsVersion().equalsIgnoreCase("v1_15_R1")) {
            return new OldFakePlayerSynchronizer(npc);
        } else {
            return new NewFakePlayerSynchronizer(npc);
        }
    }
}
