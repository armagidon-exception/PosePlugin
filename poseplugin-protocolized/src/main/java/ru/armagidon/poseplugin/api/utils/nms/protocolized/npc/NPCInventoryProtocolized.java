package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.entity.Player;
import ru.armagidon.poseplugin.api.utils.misc.DataTable;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCInventory;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.New.NewNPCInventory;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.Old.OldNPCInventory;
import ru.armagidon.poseplugin.api.utils.versions.Version;


public final class NPCInventoryProtocolized
{

    public static NPCInventory<WrappedDataWatcher> createInventory(FakePlayer<WrappedDataWatcher> fakePlayer) {
        return switch (Version.getVersion()){
            case v1_15 -> new OldNPCInventory(fakePlayer);
            default -> new NewNPCInventory(fakePlayer);
        };
    }
}
