package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.Old;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.misc.NBTModifier;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.NPCSynchronizerProtocolized;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.PacketContainer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityEquipment;

import java.util.Arrays;

public class OldNPCSynchronizer extends NPCSynchronizerProtocolized {

    public OldNPCSynchronizer(FakePlayer<WrappedDataWatcher> fakePlayer) {
        super(fakePlayer);
    }

    public void updateEquipment(){
        PacketContainer<WrapperPlayServerEntityEquipment> container = resetEquipment(fakePlayer.getParent().getEquipment());
        fakePlayer.getTrackers().forEach(container::send);
    }

    private PacketContainer<WrapperPlayServerEntityEquipment> resetEquipment(EntityEquipment equipment){
        WrapperPlayServerEntityEquipment[] eq = Arrays.stream(EnumWrappers.ItemSlot.values()).
                map(slot -> {
                    ItemStack itemStack = OldNPCSynchronizer.this.getEquipmentBySlot(equipment, slot);
                    if (slot != EnumWrappers.ItemSlot.MAINHAND && slot != EnumWrappers.ItemSlot.OFFHAND) {
                        NBTModifier.remove(itemStack, PosePluginAPI.NBT_TAG);
                    }
                    WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment();
                    packet.setEntityID(fakePlayer.getId());
                    packet.setItem(itemStack);
                    packet.setSlot(slot);
                    return packet;
                }).
                toArray(WrapperPlayServerEntityEquipment[]::new);
        return new PacketContainer<>(eq);
    }

    @Override
    public void syncEquipment() {
        updateEquipment();
    }
}
