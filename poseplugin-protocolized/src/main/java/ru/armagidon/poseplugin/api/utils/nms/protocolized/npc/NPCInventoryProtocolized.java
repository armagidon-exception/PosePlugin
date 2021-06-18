package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCInventory;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.New.NewNPCInventory;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.Old.OldNPCInventory;
import ru.armagidon.poseplugin.api.utils.versions.VersionControl;


public class NPCInventoryProtocolized extends NPCInventory<WrappedDataWatcher>
{

    private final NPCInventory<WrappedDataWatcher> equipmentManager;

    public NPCInventoryProtocolized(FakePlayer<WrappedDataWatcher> fakePlayer) {
        super(fakePlayer);
        if (VersionControl.getMCVersion() == 1) {
            this.equipmentManager = new OldNPCInventory(fakePlayer);
        } else {
            this.equipmentManager = new NewNPCInventory(fakePlayer);
        }
    }

    @Override
    public void showEquipment(Player receiver) {
        this.equipmentManager.showEquipment(receiver);
    }

    @Override
    public void mergeCustomEquipmentPacket() {
        equipmentManager.mergeCustomEquipmentPacket();
    }
}
