package ru.armagidon.poseplugin.api.utils.npc;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.nms.ReflectionTools;
import ru.armagidon.poseplugin.api.utils.npc.protocolized.New.NewEquipmentManager;
import ru.armagidon.poseplugin.api.utils.npc.protocolized.old.OldCustomEquipmentManager;

public interface FakePlayerCustomEquipmentManager
{
    void showEquipment(Player receiver);

    void setPieceOfEquipment(EquipmentSlot slotType, ItemStack stack);

    void removePieceOfEquipment(EquipmentSlot slot);

    static FakePlayerCustomEquipmentManager createNew(FakePlayer npc) {
        if (ReflectionTools.nmsVersion().equalsIgnoreCase("v1_15_R1")) {
            return new OldCustomEquipmentManager(npc);
        } else {
            return new NewEquipmentManager(npc);
        }
    }
}
