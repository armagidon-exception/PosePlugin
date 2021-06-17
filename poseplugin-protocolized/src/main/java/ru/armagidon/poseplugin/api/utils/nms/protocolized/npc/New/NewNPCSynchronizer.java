package ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.New;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.misc.NBTModifier;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.nms.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.npc.NPCSynchronizerProtocolized;
import ru.armagidon.poseplugin.api.utils.nms.protocolized.wrappers.WrapperPlayServerEntityEquipment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NewNPCSynchronizer extends NPCSynchronizerProtocolized {



    public NewNPCSynchronizer(FakePlayer<WrappedDataWatcher> fakePlayer) {
        super(fakePlayer);/*((EntityPlayer)asNMSCopy(fakePlayer.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(16));*/
    }

    public void syncEquipment(){
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slots =
                Arrays.stream(EnumWrappers.ItemSlot.values()).map(slot -> {
                    if( !slot.equals(EnumWrappers.ItemSlot.MAINHAND) && !slot.equals(EnumWrappers.ItemSlot.OFFHAND) ) {
                        ItemStack i = getEquipmentBySlot(fakePlayer.getParent().getEquipment(), slot);
                        NBTModifier.remove(i, PosePluginAPI.NBT_TAG);
                        return new Pair<>(slot, i);
                    } else {
                        return new Pair<>(slot, getEquipmentBySlot(fakePlayer.getParent().getEquipment(), slot));
                    }
                }).collect(Collectors.toList());
        WrapperPlayServerEntityEquipment eq = new WrapperPlayServerEntityEquipment(/*npc.getFake().getId(), slots*/);
        eq.setSlotStackPairsList(slots);
        eq.setEntityID(fakePlayer.getId());
        fakePlayer.getTrackers().forEach(eq::sendPacket);
    }
}
