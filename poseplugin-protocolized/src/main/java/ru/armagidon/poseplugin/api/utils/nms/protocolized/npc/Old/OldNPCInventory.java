package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.Old;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.misc.DataTable;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.npc.NPCInventory;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.PacketContainer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityEquipment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class OldNPCInventory extends NPCInventory<WrappedDataWatcher>
{

    private PacketContainer<WrapperPlayServerEntityEquipment> customEquipmentPacket;

    public OldNPCInventory(FakePlayer<WrappedDataWatcher> fakePlayer) {
        super(fakePlayer);
    }

    @Override
    public void show(Player receiver) {
        customEquipmentPacket.send(receiver);
    }

    public void mergeCustomEquipmentPacket() {
        customEquipmentPacket = combine(getCustomEquipment().getAll());
    }

    @Override
    public void update() {
        PacketContainer<WrapperPlayServerEntityEquipment> updatePacket = combine(getCustomEquipment().getDirty());
        fakePlayer.getTrackers().forEach(updatePacket::send);
    }

    private PacketContainer<WrapperPlayServerEntityEquipment> combine(Collection<DataTable.DataObject<EquipmentSlot, ItemStack>> collection) {
        return collection.stream().
                map(entry -> new WrapperPlayServerEntityEquipment()
                        .setEntityID(fakePlayer.getId())
                        .setSlot(EnumWrappers.ItemSlot.values()[entry.getKey().ordinal()])
                        .setItem(entry.getValue())).collect(Collectors.collectingAndThen(Collectors.toList(), PacketContainer::new));
    }
}
