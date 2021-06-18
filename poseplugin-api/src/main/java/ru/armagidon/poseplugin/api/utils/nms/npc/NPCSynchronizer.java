package ru.armagidon.poseplugin.api.utils.nms.npc;

import lombok.AllArgsConstructor;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
public abstract class NPCSynchronizer<T>
{

    protected final Set<EquipmentSlot> ignoredSlots = new HashSet<>();

    protected final FakePlayer<T> fakePlayer;

    public abstract void syncHeadRotation();

    public abstract void syncOverlays();

    public abstract void syncEquipment();

    public void ignoreSlot(EquipmentSlot slot) {
        ignoredSlots.add(slot);
    }
}
