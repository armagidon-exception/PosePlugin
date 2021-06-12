package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import ru.armagidon.poseplugin.api.utils.nms.ToolPackage;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCSynchronizer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.New.NewNPCSynchronizer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.Old.OldNPCSynchronizer;
import ru.armagidon.poseplugin.api.utils.versions.VersionControl;


public class NPCSynchronizedProtocolized extends NPCSynchronizer<WrappedDataWatcher>
{

    private final NPCSynchronizer<WrappedDataWatcher> synchronizer;

    public NPCSynchronizedProtocolized(FakePlayer<WrappedDataWatcher> fakePlayer) {
        super(fakePlayer);
        if (VersionControl.getMCVersion() == 1) {
            this.synchronizer = new OldNPCSynchronizer(fakePlayer);
        } else {
            this.synchronizer = new NewNPCSynchronizer(fakePlayer);
        }
    }

    @Override
    public void syncHeadRotation() {
        synchronizer.syncHeadRotation();
    }

    @Override
    public void syncOverlays() {
        synchronizer.syncOverlays();
    }

    @Override
    public void syncEquipment() {
        synchronizer.syncEquipment();
    }
}
