package ru.armagidon.poseplugin.api.utils.npc.protocolized.old;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import ru.armagidon.poseplugin.api.utils.misc.NBTModifier;
import ru.armagidon.poseplugin.api.PosePluginAPI;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayer;
import ru.armagidon.poseplugin.api.utils.npc.FakePlayerSynchronizer;
import ru.armagidon.poseplugin.api.utils.npc.protocolized.PacketContainer;
import ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerEntityEquipment;
import ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerEntityHeadRotation;
import ru.armagidon.poseplugin.api.wrappers.WrapperPlayServerRelEntityMoveLook;

import java.util.Arrays;

import static ru.armagidon.poseplugin.api.utils.npc.FakePlayerUtils.getFixedRotation;

public class OldFakePlayerSynchronizer implements FakePlayerSynchronizer {

    private final FakePlayer npc;

    private byte pOverlays;

    public OldFakePlayerSynchronizer(FakePlayer npc) {
        this.npc = npc;
        this.pOverlays = npc.getDataWatcher().getByte(16);
    }

    public void updateEquipment(){
        PacketContainer<WrapperPlayServerEntityEquipment> container = resetEquipment(npc.getParent().getEquipment());
        npc.getTrackers().forEach(container::send);
    }

    private PacketContainer<WrapperPlayServerEntityEquipment> resetEquipment(EntityEquipment equipment){
        WrapperPlayServerEntityEquipment[] eq = Arrays.stream(EnumWrappers.ItemSlot.values()).
                map(slot -> {
                    ItemStack itemStack = OldFakePlayerSynchronizer.this.getEquipmentBySlot(equipment, slot);
                    if (slot != EnumWrappers.ItemSlot.MAINHAND && slot != EnumWrappers.ItemSlot.OFFHAND) {
                        NBTModifier.remove(itemStack, PosePluginAPI.NBT_TAG);
                    }
                    WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment();
                    packet.setEntityID(npc.getId());
                    packet.setItem(itemStack);
                    packet.setSlot(slot);
                    return packet;
                }).
                toArray(WrapperPlayServerEntityEquipment[]::new);
        return new PacketContainer<>(eq);
    }

    public void updateOverlays(){
        byte overlays = WrappedDataWatcher.getEntityWatcher(npc.getParent()).getByte(16);/*((EntityPlayer) NMSUtils.asNMSCopy(npc.getParent())).getDataWatcher().get(DataWatcherRegistry.a.a(16));*/
        if(overlays != pOverlays){
            pOverlays = overlays;
            npc.getMetadataAccessor().setOverlays(pOverlays);
            npc.getMetadataAccessor().merge(false);
            npc.getTrackers().forEach(p -> npc.getMetadataAccessor().showPlayer(p));
        }
    }

    public void updateHeadRotation() {
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
        ItemStack eq;
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

    @Override
    public void syncHeadRotation() {
        updateHeadRotation();
    }

    @Override
    public void syncOverlays() {
        updateOverlays();
    }

    @Override
    public void syncEquipment() {
        updateEquipment();
    }
}
