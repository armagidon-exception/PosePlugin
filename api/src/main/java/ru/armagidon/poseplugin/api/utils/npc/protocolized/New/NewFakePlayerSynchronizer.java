package ru.armagidon.poseplugin.api.utils.npc.protocolized.New;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.armagidonapi.itemutils.nbt.NBTModifier;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayerSynchronizer;
import ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerEntityEquipment;
import ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerEntityHeadRotation;
import ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerRelEntityMoveLook;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.armagidon.poseplugin.api.utils.npc.FakePlayerUtils.getFixedRotation;

public class NewFakePlayerSynchronizer implements FakePlayerSynchronizer {

    private final FakePlayer npc;

    private byte pOverlays;

    public NewFakePlayerSynchronizer(FakePlayer npc) {
        this.npc = npc;
        this.pOverlays = npc.getDataWatcher().getByte(16);/*((EntityPlayer)asNMSCopy(npc.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(16));*/
    }

    public void syncEquipment(){
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slots =
                Arrays.stream(EnumWrappers.ItemSlot.values()).map(slot -> {
                    if( !slot.equals(EnumWrappers.ItemSlot.MAINHAND) && !slot.equals(EnumWrappers.ItemSlot.OFFHAND) ) {
                        ItemStack i = getEquipmentBySlot(npc.getParent().getEquipment(), slot);
                        NBTModifier.remove(i, PosePluginAPI.NBT_TAG);
                        return new Pair<>(slot, i);
                    } else {
                        return new Pair<>(slot, getEquipmentBySlot(npc.getParent().getEquipment(), slot));
                    }
                }).collect(Collectors.toList());
        WrapperPlayServerEntityEquipment eq = new WrapperPlayServerEntityEquipment(/*npc.getFake().getId(), slots*/);
        eq.setSlotStackPairsList(slots);
        eq.setEntityID(npc.getId());
        npc.getTrackers().forEach(eq::sendPacket);
    }

    public void syncOverlays(){
        byte overlays = WrappedDataWatcher.getEntityWatcher(npc.getParent()).getByte(16);//((EntityPlayer) NMSUtils.asNMSCopy(npc.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(16));
        if(overlays != pOverlays){
            pOverlays = overlays;
            npc.getMetadataAccessor().setOverlays(pOverlays);
            npc.getMetadataAccessor().merge(true);
            npc.getTrackers().forEach(p -> npc.getMetadataAccessor().showPlayer(p));
        }
    }

    public void syncHeadRotation() {
        WrapperPlayServerEntityHeadRotation rotation = new WrapperPlayServerEntityHeadRotation(/*npc.getFake(), getFixedRotation(npc.getParent().getLocation().getYaw())*/);
        rotation.setEntityID(npc.getId());
        rotation.setHeadYaw(getFixedRotation(npc.getParent().getLocation().getYaw()));
        WrapperPlayServerRelEntityMoveLook lookPacket = new WrapperPlayServerRelEntityMoveLook(/*npc.getFake().getId(), (short) 0, (short) 0, (short) 0, getFixedRotation(npc.getParent().getLocation().getYaw()), (byte) 0, true*/);
        lookPacket.setEntityID(npc.getId());
        lookPacket.setYaw(npc.getParent().getLocation().getYaw());
        lookPacket.setOnGround(true);
        npc.getTrackers().forEach(p -> {
            lookPacket.sendPacket(p);
            rotation.sendPacket(p);
        });
    }

    private ItemStack getEquipmentBySlot(EntityEquipment e, EnumWrappers.ItemSlot slot){
        org.bukkit.inventory.ItemStack eq;
        switch (slot){
            case HEAD:
                eq = e.getHelmet();
                break;
            case CHEST:
                eq = e.getChestplate();
                break;
            case LEGS:
                eq = e.getLeggings();
                break;
            case FEET:
                eq = e.getBoots();
                break;
            case OFFHAND:
                eq = e.getItemInOffHand();
                break;
            default:
                eq = e.getItemInMainHand();
        }
        return eq;
    }
}
