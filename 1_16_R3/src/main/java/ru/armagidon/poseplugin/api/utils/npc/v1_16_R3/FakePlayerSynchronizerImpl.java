package ru.armagidon.poseplugin.api.utils.npc.v1_16_R3;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import ru.armagidon.armagidonapi.itemutils.nbt.NBTModifier;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.nms.NMSUtils;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayerSynchronizer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.asNMSCopy;
import static ru.armagidon.poseplugin.api.utils.nms.NMSUtils.sendPacket;
import static ru.armagidon.poseplugin.api.utils.npc.FakePlayerUtils.getEquipmentBySlot;
import static ru.armagidon.poseplugin.api.utils.npc.FakePlayerUtils.getFixedRotation;

public class FakePlayerSynchronizerImpl implements FakePlayerSynchronizer {

    private final FakePlayer npc;

    private byte pOverlays;

    public FakePlayerSynchronizerImpl(FakePlayer npc) {
        this.npc = npc;
        this.pOverlays = ((EntityPlayer)asNMSCopy(npc.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(16));
    }

    public void syncEquipment(){
        List<Pair<EnumItemSlot, ItemStack>> slots =
                Arrays.stream(EnumItemSlot.values()).map(slot-> {
                    if( !slot.equals(EnumItemSlot.MAINHAND) && !slot.equals(EnumItemSlot.OFFHAND) ) {
                        org.bukkit.inventory.ItemStack i = getEquipmentBySlot(npc.getParent().getEquipment(), slot);
                        NBTModifier.remove(i, PosePluginAPI.NBT_TAG);
                        //PosePluginAPI.pluginTagClear.pushThrough(i);
                        return Pair.of(slot, CraftItemStack.asNMSCopy(i));
                    } else {
                        return Pair.of(slot, CraftItemStack.asNMSCopy(getEquipmentBySlot(npc.getParent().getEquipment(), slot)));
                    }
                }).collect(Collectors.toList());
        PacketPlayOutEntityEquipment eq = new PacketPlayOutEntityEquipment(npc.getFake().getId(), slots);
        npc.getTrackers().forEach(r->sendPacket(r,eq));
    }

    public void syncOverlays(){
        byte overlays = ((EntityPlayer) NMSUtils.asNMSCopy(npc.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(16));
        if(overlays!=pOverlays){
            pOverlays = overlays;
            npc.getMetadataAccessor().setOverlays(pOverlays);
            npc.getMetadataAccessor().merge(false);
            npc.getTrackers().forEach(p-> npc.getMetadataAccessor().showPlayer(p));
        }
    }

    public void syncHeadRotation() {
        PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(npc.getFake(), getFixedRotation(npc.getParent().getLocation().getYaw()));
        PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook lookPacket = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(npc.getFake().getId(), (short) 0, (short) 0, (short) 0, getFixedRotation(npc.getParent().getLocation().getYaw()), (byte) 0, true);
        npc.getTrackers().forEach(p -> {
            NMSUtils.sendPacket(p, lookPacket);
            NMSUtils.sendPacket(p, rotation);
        });
    }
}
